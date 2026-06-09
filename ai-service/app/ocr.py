"""
OCR 手写字符识别 Blueprint
POST /ocr/recognize — 上传手写图片，返回逐字符识别结果
模型: ONNX EfficientNet，62 类字符 (0-9 A-Z a-z)
"""
import traceback
import cv2
import numpy as np
from flask import Blueprint, request, jsonify

ocr_bp = Blueprint("ocr", __name__)

# ── 62 类字符顺表 ──────────────────────────────────────────────────
EMNIST_CHARS = (
    "0123456789"
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    "abcdefghijklmnopqrstuvwxyz"
)
EMNIST_MEAN = 0.1736
EMNIST_STD = 0.3317


def _get_session():
    """从 Flask app.config 获取已预加载的 OCR 推理会话"""
    from flask import current_app
    return current_app.config["_models"].get("ocr")


# ── 图像预处理工具函数 ────────────────────────────────────────────

def _read_image_from_request(file_key="image"):
    """从 multipart 请求中读取图片为 OpenCV BGR 格式"""
    if file_key not in request.files:
        return None
    file = request.files[file_key]
    if file.filename == "":
        return None
    img_bytes = file.read()
    nparr = np.frombuffer(img_bytes, np.uint8)
    return cv2.imdecode(nparr, cv2.IMREAD_COLOR)


def _preprocess_char_image(char_roi, target_size=(28, 28)):
    """将单个字符 ROI 处理为 ONNX 模型输入 tensor (1, 1, 28, 28) float32"""
    if len(char_roi.shape) == 3:
        gray = cv2.cvtColor(char_roi, cv2.COLOR_BGR2GRAY)
    else:
        gray = char_roi

    resized = cv2.resize(gray, target_size, interpolation=cv2.INTER_AREA)
    img_f32 = resized.astype(np.float32) / 255.0
    img_f32 = (img_f32 - EMNIST_MEAN) / EMNIST_STD
    return np.expand_dims(np.expand_dims(img_f32, axis=0), axis=0)


def _segment_connected_components(binary):
    """连通域分割 — 返回由左到右排序的字符 ROI 列表"""
    num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(
        binary, connectivity=8
    )
    components = []
    for i in range(1, num_labels):
        x, y, w, h, area = stats[i]
        if area < 20 or h < 5 or w < 3:
            continue
        roi = binary[y:y + h, x:x + w]
        components.append({"x": x, "roi": roi})
    components.sort(key=lambda c: c["x"])
    return components


# ── 核心接口 ──────────────────────────────────────────────────────

@ocr_bp.route("/ocr/recognize", methods=["POST"])
def ocr_recognize():
    """
    POST /ocr/recognize
    multipart/form-data: image=<图片文件>
    返回: {"text": "...", "chars": ["A","B"], "confidence": [0.98, 0.95]}
    """
    try:
        session = _get_session()
        if session is None:
            return jsonify({"error": "OCR 模型未加载", "text": "", "chars": []}), 503

        img = _read_image_from_request("image")
        if img is None:
            return jsonify({"error": "缺少图片文件（字段名: image）", "text": "", "chars": []}), 400

        # 灰度化 + Otsu 二值化（文字前景为白色）
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        _, binary = cv2.threshold(
            gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU
        )

        # 连通域分割
        components = _segment_connected_components(binary)
        if not components:
            return jsonify({"text": "", "chars": [], "confidence": []})

        input_name = session.get_inputs()[0].name
        chars = []
        confidences = []

        for comp in components:
            tensor = _preprocess_char_image(comp["roi"])
            logits = session.run(None, {input_name: tensor})[0]  # (1, 62)
            probs = np.exp(logits - np.max(logits, axis=1, keepdims=True))
            probs = probs / np.sum(probs, axis=1, keepdims=True)
            idx = int(np.argmax(logits, axis=1)[0])
            chars.append(EMNIST_CHARS[idx])
            confidences.append(round(float(probs[0][idx]), 4))

        text = "".join(chars)
        print(f"[OCR] 识别结果: {text}")
        return jsonify({"text": text, "chars": chars, "confidence": confidences})

    except Exception:
        print(f"[OCR] 识别异常:\n{traceback.format_exc()}")
        return jsonify({"error": "OCR 识别服务内部异常", "text": "", "chars": []}), 500
