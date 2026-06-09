"""
试卷版面检测 Blueprint
POST /layout/detect    — 检测试卷版面区域（YOLOv8 paper-8n）
POST /layout/compare   — 比较两份试卷作答区域相似度
模型: YOLOv8，类别: Text/Title/Figure/Table 等
"""
import os
import traceback
import cv2
import numpy as np
from flask import Blueprint, request, jsonify, current_app

layout_bp = Blueprint("layout", __name__)


def _get_model():
    """获取已预加载的试卷版面检测 YOLO 模型"""
    return current_app.config["_models"].get("layout")


def _read_image_from_request(file_key="image"):
    """从 multipart 请求中读取图片"""
    if file_key not in request.files:
        return None
    file = request.files[file_key]
    if file.filename == "":
        return None
    img_bytes = file.read()
    nparr = np.frombuffer(img_bytes, np.uint8)
    return cv2.imdecode(nparr, cv2.IMREAD_COLOR)


def _load_image_from_path(file_url):
    """根据相对路径 file_url 加载图片"""
    backend_dir = current_app.config["BACKEND_DIR"]
    # 去掉可能的开头的 / 或 \
    clean_url = file_url.lstrip("/\\")
    full_path = os.path.join(backend_dir, clean_url)
    if not os.path.exists(full_path):
        raise FileNotFoundError(f"图片文件不存在: {full_path}")
    return cv2.imread(full_path, cv2.IMREAD_COLOR)


def _compute_anomaly_score(boxes, image_width, image_height):
    """
    根据检测框计算版面异常分数 (0~1)
    规则:
      - Text 区域面积占比 < 10% → +0.3
      - 检测框总数 < 3 → +0.3
      - 最终 clamp 到 [0, 1]
    """
    score = 0.0

    # 计算 Text 区域占比
    text_boxes = [b for b in boxes if b.get("label", "").lower() == "text"]
    total_area = image_width * image_height
    text_area = 0
    for b in text_boxes:
        w = b["x2"] - b["x1"]
        h = b["y2"] - b["y1"]
        text_area += max(0, w * h)

    text_ratio = text_area / total_area if total_area > 0 else 0
    if text_ratio < 0.10:
        score += 0.3

    # 检测框数量
    if len(boxes) < 3:
        score += 0.3

    return min(max(score, 0.0), 1.0)


# ── /layout/detect ────────────────────────────────────────────────

@layout_bp.route("/layout/detect", methods=["POST"])
def layout_detect():
    """
    POST /layout/detect
    multipart/form-data: image=<图片文件>, [confidence=0.35]
    返回: {"boxes": [...], "anomaly_score": 0.3, "image_size": {...}}
    """
    try:
        model = _get_model()
        if model is None:
            return jsonify({"error": "版面检测模型未加载"}), 503

        img = _read_image_from_request("image")
        if img is None:
            return jsonify({"error": "缺少图片文件（字段名: image）"}), 400

        confidence = float(request.form.get("confidence", 0.35))
        h, w = img.shape[:2]

        results = model(img, conf=confidence, verbose=False)

        boxes = []
        for r in results:
            if r.boxes is None:
                continue
            for box in r.boxes:
                cls_id = int(box.cls[0].item())
                label = model.names.get(cls_id, str(cls_id))
                conf = float(box.conf[0].item())
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                boxes.append({
                    "label": label,
                    "confidence": round(conf, 4),
                    "x1": round(x1, 1),
                    "y1": round(y1, 1),
                    "x2": round(x2, 1),
                    "y2": round(y2, 1),
                })

        anomaly_score = round(_compute_anomaly_score(boxes, w, h), 4)

        print(f"[layout] 版面检测完成: {len(boxes)} 个区域, anomaly={anomaly_score}")
        return jsonify({
            "boxes": boxes,
            "anomaly_score": anomaly_score,
            "image_size": {"width": w, "height": h}
        })

    except Exception:
        print(f"[layout] 版面检测异常:\n{traceback.format_exc()}")
        return jsonify({"error": "版面检测服务内部异常"}), 500


# ── /layout/compare ──────────────────────────────────────────────

@layout_bp.route("/layout/compare", methods=["POST"])
def layout_compare():
    """
    POST /layout/compare
    JSON: {"file_url_1": "相对路径1", "file_url_2": "相对路径2"}
    比较两份试卷作答区域相似度
    返回: {"similarity": 0.85, "is_suspicious": true, "detail": "作答区域高度重合"}
    """
    try:
        model = _get_model()
        if model is None:
            return jsonify({"error": "版面检测模型未加载"}), 503

        data = request.get_json(force=True)
        url1 = data.get("file_url_1", "")
        url2 = data.get("file_url_2", "")

        if not url1 or not url2:
            return jsonify({"error": "缺少 file_url_1 或 file_url_2"}), 400

        # 加载两张图片
        img1 = _load_image_from_path(url1)
        img2 = _load_image_from_path(url2)

        if img1 is None or img2 is None:
            return jsonify({"error": "无法读取图片文件"}), 400

        # 对两张图分别检测
        def get_text_boxes(model, img):
            results = model(img, conf=0.35, verbose=False)
            boxes = []
            for r in results:
                if r.boxes is None:
                    continue
                for box in r.boxes:
                    cls_id = int(box.cls[0].item())
                    label = model.names.get(cls_id, str(cls_id))
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    boxes.append({
                        "label": label,
                        "x1": x1, "y1": y1, "x2": x2, "y2": y2
                    })
            return boxes

        boxes1 = get_text_boxes(model, img1)
        boxes2 = get_text_boxes(model, img2)

        # 计算检测框分布的余弦相似度（基于位置和大小特征）
        def boxes_to_feature(boxes, img_w, img_h):
            """将 boxes 转为归一化特征向量"""
            if not boxes:
                return np.zeros(4)
            features = []
            for b in boxes:
                x1, y1, x2, y2 = b["x1"], b["y1"], b["x2"], b["y2"]
                cx = (x1 + x2) / 2 / img_w
                cy = (y1 + y2) / 2 / img_h
                bw = (x2 - x1) / img_w
                bh = (y2 - y1) / img_h
                features.extend([cx, cy, bw, bh])
            # 取平均特征
            arr = np.array(features).reshape(-1, 4).mean(axis=0)
            return arr

        feat1 = boxes_to_feature(boxes1, img1.shape[1], img1.shape[0])
        feat2 = boxes_to_feature(boxes2, img2.shape[1], img2.shape[0])

        # 余弦相似度
        norm1 = np.linalg.norm(feat1)
        norm2 = np.linalg.norm(feat2)
        if norm1 == 0 or norm2 == 0:
            similarity = 0.0
        else:
            similarity = float(np.dot(feat1, feat2) / (norm1 * norm2))
            similarity = max(0.0, min(1.0, similarity))

        is_suspicious = similarity > 0.8
        detail = "作答区域高度重合" if is_suspicious else "作答区域差异较大"

        print(f"[layout] 版面比较完成: similarity={similarity:.4f}, suspicious={is_suspicious}")
        return jsonify({
            "similarity": round(similarity, 4),
            "is_suspicious": is_suspicious,
            "detail": detail
        })

    except FileNotFoundError as e:
        return jsonify({"error": str(e)}), 404
    except Exception:
        print(f"[layout] 版面比较异常:\n{traceback.format_exc()}")
        return jsonify({"error": "版面比较服务内部异常"}), 500


# ── /layout/analyze-and-ocr ─────────────────────────────────────

@layout_bp.route("/layout/analyze-and-ocr", methods=["POST"])
def layout_analyze_and_ocr():
    """
    POST /layout/analyze-and-ocr
    multipart/form-data: image=<图片文件>, [confidence=0.25]

    一体化流水线: 版面检测 → 裁剪作答区域 → 逐区域 OCR
    返回: layout_boxes + ocr_regions + combined_text
    """
    try:
        layout_model = _get_model()
        if layout_model is None:
            return jsonify({"error": "版面检测模型未加载"}), 503

        img = _read_image_from_request("image")
        if img is None:
            return jsonify({"error": "缺少图片文件（字段名: image）"}), 400

        confidence = float(request.form.get("confidence", 0.25))
        h, w = img.shape[:2]

        # ── 1. 版面检测 ──
        results = layout_model(img, conf=confidence, verbose=False)

        layout_boxes = []
        for r in results:
            if r.boxes is None:
                continue
            for box in r.boxes:
                cls_id = int(box.cls[0].item())
                label = layout_model.names.get(cls_id, str(cls_id))
                conf = float(box.conf[0].item())
                x1, y1, x2, y2 = box.xyxy[0].tolist()
                layout_boxes.append({
                    "label": label,
                    "confidence": round(conf, 4),
                    "x1": round(x1, 1),
                    "y1": round(y1, 1),
                    "x2": round(x2, 1),
                    "y2": round(y2, 1),
                })

        # ── 2. 过滤作答区域 (Text + Figure 等可能含手写答案的区域) ──
        text_regions = [b for b in layout_boxes
                        if b["label"].lower() in ("text", "title", "figure", "figure caption")]

        # ── 3. 裁剪 + OCR ──
        ocr_regions = []
        all_texts = []

        # 获取 OCR 模型
        from flask import current_app
        ocr_session = current_app.config["_models"].get("ocr")

        if ocr_session and text_regions:
            from app.ocr import _preprocess_char_image, _segment_connected_components
            EMNIST_CHARS = (
                "0123456789"
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                "abcdefghijklmnopqrstuvwxyz"
            )
            input_name = ocr_session.get_inputs()[0].name

            for region in text_regions:
                x1 = max(0, int(region["x1"]))
                y1 = max(0, int(region["y1"]))
                x2 = min(w, int(region["x2"]))
                y2 = min(h, int(region["y2"]))

                if x2 - x1 < 10 or y2 - y1 < 10:
                    continue

                # 裁剪区域
                crop = img[y1:y2, x1:x2]
                gray = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)

                # Otsu 二值化
                _, binary = cv2.threshold(
                    gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU
                )

                # 连通域分割
                components = _segment_connected_components(binary)

                chars = []
                confidences = []
                for comp in components:
                    tensor = _preprocess_char_image(comp["roi"])
                    logits = ocr_session.run(None, {input_name: tensor})[0]
                    probs = np.exp(logits - np.max(logits, axis=1, keepdims=True))
                    probs = probs / np.sum(probs, axis=1, keepdims=True)
                    idx = int(np.argmax(logits, axis=1)[0])
                    chars.append(EMNIST_CHARS[idx])
                    confidences.append(round(float(probs[0][idx]), 4))

                region_text = "".join(chars)
                all_texts.append(region_text)

                ocr_regions.append({
                    "box": region,
                    "ocr_text": region_text,
                    "ocr_chars": chars,
                    "ocr_confidence": round(float(np.mean(confidences)), 4) if confidences else 0,
                })

        combined_text = " ".join(all_texts)

        # ── 4. 异常评分 ──
        anomaly_score = round(_compute_anomaly_score(layout_boxes, w, h), 4)

        print(f"[layout] analyze+OCR: {len(layout_boxes)} boxes, "
              f"{len(ocr_regions)} OCR regions, text='{combined_text[:80]}'")

        return jsonify({
            "layout_boxes": layout_boxes,
            "ocr_regions": ocr_regions,
            "combined_text": combined_text,
            "anomaly_score": anomaly_score,
            "image_size": {"width": w, "height": h}
        })

    except Exception:
        print(f"[layout] analyze+OCR 异常:\n{traceback.format_exc()}")
        return jsonify({"error": "版面分析+OCR 服务内部异常"}), 500
