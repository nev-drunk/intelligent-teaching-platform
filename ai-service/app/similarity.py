"""
文本相似度 Blueprint
POST /similarity/compare  — 比较两段文本相似度
POST /similarity/top3     — 从候选列表中返回 Top3 最相似项
模型: Siamese BERT (sbert-tiny-chinese + 余弦相似度)
绝对不使用向量数据库，每次请求实时计算
"""
import os
import traceback

import torch
import torch.nn as nn
from torch.nn import CosineSimilarity
from transformers import BertModel, BertTokenizer

from flask import Blueprint, request, jsonify, current_app

similarity_bp = Blueprint("similarity", __name__)


# ── Siamese BERT 网络定义 ────────────────────────────────────────

class SiameseBert(nn.Module):
    """Siamese BERT — 使用预训练 BERT + 余弦相似度"""

    def __init__(self, model_name="bert-base-chinese", hidden_dim=768):
        super().__init__()
        self.bert: BertModel = BertModel.from_pretrained(model_name)
        self.projection: nn.Linear = nn.Linear(hidden_dim, hidden_dim)
        self.cosine: CosineSimilarity = CosineSimilarity(dim=1)

    def forward(self, input_ids_a, attention_mask_a, input_ids_b, attention_mask_b):
        emb_a = self._encode(input_ids_a, attention_mask_a)
        emb_b = self._encode(input_ids_b, attention_mask_b)
        sim = self.cosine(emb_a, emb_b)
        return (sim + 1.0) / 2.0

    def _encode(self, input_ids, attention_mask):
        out = self.bert(input_ids=input_ids, attention_mask=attention_mask)
        pooled = out.pooler_output
        return self.projection(pooled)


def _init_similarity_model(models_dir):
    """
    供 create_app() 调用的初始化函数
    返回: (model, tokenizer, optimal_threshold)
    """
    model_path = os.path.join(models_dir, "sbert", "siamese_sbert_best.pt")

    checkpoint = torch.load(model_path, map_location="cpu")
    model = SiameseBert()

    # 从 checkpoint 中提取 model_state_dict
    if "model_state_dict" in checkpoint:
        state_dict = checkpoint["model_state_dict"]
    else:
        state_dict = checkpoint

    # 过滤不匹配的键
    model_state = model.state_dict()
    filtered = {
        k: v for k, v in state_dict.items()
        if k in model_state and model_state[k].shape == v.shape
    }
    model.load_state_dict(filtered, strict=False)
    model.eval()

    # 加载分词器
    tokenizer = BertTokenizer.from_pretrained("bert-base-chinese")

    # 获取最佳阈值
    threshold = checkpoint.get("optimal_threshold", 0.7)

    return model, tokenizer, threshold


def _get_model_and_tokenizer():
    """从 app.config 获取预加载的模型和分词器"""
    models = current_app.config["_models"]
    return models.get("sbert"), models.get("sbert_tokenizer"), models.get("sbert_threshold", 0.7)


def _compute_similarity(text1, text2, model, tokenizer):
    """计算两段文本的余弦相似度 (0~1)"""
    encoded = tokenizer(
        [text1, text2],
        padding=True,
        truncation=True,
        max_length=256,
        return_tensors="pt",
    )

    input_ids_a = encoded["input_ids"][0:1]
    attention_mask_a = encoded["attention_mask"][0:1]
    input_ids_b = encoded["input_ids"][1:2]
    attention_mask_b = encoded["attention_mask"][1:2]

    with torch.no_grad():
        score = model(input_ids_a, attention_mask_a, input_ids_b, attention_mask_b)

    return float(score.item())


# ── /similarity/compare ─────────────────────────────────────────

@similarity_bp.route("/similarity/compare", methods=["POST"])
def similarity_compare():
    """
    POST /similarity/compare
    JSON: {"text1": "文本A", "text2": "文本B"}
    返回: {"score": 0.85, "is_similar": true}
    """
    try:
        model, tokenizer, threshold = _get_model_and_tokenizer()
        if model is None or tokenizer is None:
            return jsonify({"error": "相似度模型未加载"}), 503

        data = request.get_json(force=True)
        text1 = data.get("text1", "").strip()
        text2 = data.get("text2", "").strip()

        if not text1 or not text2:
            return jsonify({"error": "缺少 text1 或 text2"}), 400

        score = _compute_similarity(text1, text2, model, tokenizer)
        is_similar = score >= threshold

        print(f"[similarity] compare: score={score:.4f}, threshold={threshold:.4f}, similar={is_similar}")
        return jsonify({
            "score": round(score, 4),
            "is_similar": is_similar
        })

    except Exception:
        print(f"[similarity] 相似度计算异常:\n{traceback.format_exc()}")
        return jsonify({"error": "相似度计算服务内部异常"}), 500


# ── /similarity/top3 ────────────────────────────────────────────

@similarity_bp.route("/similarity/top3", methods=["POST"])
def similarity_top3():
    """
    POST /similarity/top3
    JSON: {"query": "新问题", "candidates": [{"id":1,"text":"历史问题1"}, ...]}
    返回: {"results": [{"id":1,"text":"历史问题1","score":0.92}, ...]}
    """
    try:
        model, tokenizer, threshold = _get_model_and_tokenizer()
        if model is None or tokenizer is None:
            return jsonify({"error": "相似度模型未加载"}), 503

        data = request.get_json(force=True)
        query = data.get("query", "").strip()
        candidates = data.get("candidates", [])

        if not query:
            return jsonify({"error": "缺少 query"}), 400
        if not candidates:
            return jsonify({"results": []})

        # 兼容两种 candidates 格式：
        # 1) [{"id": 1, "text": "..."}, ...]
        # 2) ["text1", "text2", ...]
        scored = []
        for i, cand in enumerate(candidates):
            if isinstance(cand, dict):
                text = cand.get("text", "").strip()
                cid = cand.get("id", i)
            else:
                text = str(cand).strip()
                cid = i

            if not text:
                continue
            score = _compute_similarity(query, text, model, tokenizer)
            scored.append({"id": cid, "text": text, "score": round(score, 4)})

        scored.sort(key=lambda x: x["score"], reverse=True)
        top3 = scored[:3]

        print(f"[similarity] top3: query='{query[:50]}...', results={[(s['id'], s['score']) for s in top3]}")
        return jsonify({"results": top3})

    except Exception:
        print(f"[similarity] Top3 检索异常:\n{traceback.format_exc()}")
        return jsonify({"error": "相似度检索服务内部异常"}), 500
