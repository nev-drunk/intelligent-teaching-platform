"""
Flask 应用工厂 — 预加载所有 AI 模型，注册 Blueprint
所有模型在启动时加载一次并存入 app.config，加载失败仅警告不报错
"""
import os
import logging

from flask import Flask

# ── 日志 ──────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
logger = logging.getLogger(__name__)

# ── 路径常量 ──────────────────────────────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))          # app/
AI_SERVICE_DIR = os.path.dirname(BASE_DIR)                     # ai-service/
MODELS_DIR = os.path.join(AI_SERVICE_DIR, "models")
BACKEND_DIR = os.path.join(os.path.dirname(AI_SERVICE_DIR), "backend")
TTS_DIR = os.path.join(BACKEND_DIR, "uploads", "tts")


def create_app():
    """工厂函数：创建 Flask 应用，预加载模型，注册 Blueprint"""
    app = Flask(__name__)
    app.config["MAX_CONTENT_LENGTH"] = 50 * 1024 * 1024  # 50 MB

    # CORS — 允许前端跨域调用
    from flask_cors import CORS
    CORS(app)
    app.config["MODELS_DIR"] = MODELS_DIR
    app.config["BACKEND_DIR"] = BACKEND_DIR
    app.config["TTS_DIR"] = TTS_DIR
    app.config["DASHSCOPE_API_KEY"] = os.environ.get(
        "DASHSCOPE_API_KEY",
        ""  # 请通过环境变量 DASHSCOPE_API_KEY 设置，生产环境必填
    )

    # 确保 tts 目录存在
    os.makedirs(TTS_DIR, exist_ok=True)

    # ── 预加载模型 ────────────────────────────────────────────────
    _preload_models(app)

    # ── 注册 Blueprint ────────────────────────────────────────────
    from app.ocr import ocr_bp
    from app.layout import layout_bp
    from app.courseware import courseware_bp
    from app.similarity import similarity_bp
    from app.tts import tts_bp

    app.register_blueprint(ocr_bp)
    app.register_blueprint(layout_bp)
    app.register_blueprint(courseware_bp)
    app.register_blueprint(similarity_bp)
    app.register_blueprint(tts_bp)

    # ── 静态文件服务 (TTS 音频) ───────────────────────────────────
    @app.route("/uploads/tts/<filename>")
    def serve_tts(filename):
        from flask import send_from_directory
        return send_from_directory(TTS_DIR, filename, mimetype="audio/mpeg")

    # ── 全局异常处理 ──────────────────────────────────────────────
    from flask import jsonify

    @app.errorhandler(413)
    def too_large(e):
        return jsonify({"error": "文件大小超过限制 (最大 50MB)"}), 413

    @app.errorhandler(404)
    def not_found(e):
        return jsonify({"error": "接口不存在"}), 404

    @app.errorhandler(500)
    def server_error(e):
        return jsonify({"error": "服务器内部异常"}), 500

    # ── 健康检查 ──────────────────────────────────────────────────
    @app.route("/health", methods=["GET"])
    def health():
        models_status = {k: v is not None for k, v in app.config.get("_models", {}).items()}
        return jsonify({
            "status": "ok",
            "service": "ai-service",
            "models": models_status
        })

    logger.info("=" * 60)
    logger.info("AI 微服务启动完成，已注册路由：")
    logger.info("  POST /ocr/recognize       — 手写字符识别")
    logger.info("  POST /layout/detect        — 试卷版面检测")
    logger.info("  POST /layout/compare       — 试卷版面比较")
    logger.info("  POST /courseware/detect    — 课件内容检测")
    logger.info("  POST /similarity/compare   — 文本相似度")
    logger.info("  POST /similarity/top3      — Top3 相似问题")
    logger.info("  POST /tts/synthesize       — TTS 语音合成")
    logger.info("  GET  /health               — 健康检查")
    logger.info("=" * 60)

    return app


def _preload_models(app):
    """预加载所有 AI 模型到 app.config['_models']"""
    models = {}
    app.config["_models"] = models

    # ── 1. OCR ONNX 推理会话 ─────────────────────────────────────
    try:
        import onnxruntime as ort
        model_path = os.path.join(MODELS_DIR, "ocr", "emnist_efficientnet.onnx")
        models["ocr"] = ort.InferenceSession(
            model_path, providers=["CPUExecutionProvider"]
        )
        logger.info("[INIT] OCR 模型加载成功")
    except Exception as e:
        logger.warning("[INIT] OCR 模型加载失败: %s", e)
        models["ocr"] = None

    # ── 2. 试卷版面检测 YOLOv8 ───────────────────────────────────
    try:
        from ultralytics import YOLO
        model_path = os.path.join(MODELS_DIR, "layout", "paper-8n.pt")
        models["layout"] = YOLO(model_path)
        logger.info("[INIT] 版面检测模型加载成功")
    except Exception as e:
        logger.warning("[INIT] 版面检测模型加载失败: %s", e)
        models["layout"] = None

    # ── 3. 课件内容检测 YOLOv8 ───────────────────────────────────
    try:
        from ultralytics import YOLO
        model_path = os.path.join(MODELS_DIR, "yolo", "yolo_best.pt")
        models["courseware"] = YOLO(model_path)
        logger.info("[INIT] 课件检测模型加载成功")
    except Exception as e:
        logger.warning("[INIT] 课件检测模型加载失败: %s", e)
        models["courseware"] = None

    # ── 4. Siamese BERT 相似度模型 ───────────────────────────────
    try:
        from app.similarity import _init_similarity_model
        sbert_model, tokenizer, threshold = _init_similarity_model(MODELS_DIR)
        models["sbert"] = sbert_model
        models["sbert_tokenizer"] = tokenizer
        models["sbert_threshold"] = threshold
        logger.info("[INIT] 文本相似度模型加载成功, threshold=%.4f", threshold)
    except Exception as e:
        logger.warning("[INIT] 文本相似度模型加载失败: %s", e)
        models["sbert"] = None
        models["sbert_tokenizer"] = None
        models["sbert_threshold"] = 0.7
