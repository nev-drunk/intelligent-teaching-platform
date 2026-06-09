"""
课件内容检测 Blueprint
POST /courseware/detect — 检测教师课件图片中的内容块（YOLOv8 yolo_best）
模型类别: text_block, table, diagram, formula
"""
import traceback
import cv2
import numpy as np
from flask import Blueprint, request, jsonify, current_app

courseware_bp = Blueprint("courseware", __name__)


def _get_model():
    """获取已预加载的课件检测 YOLO 模型"""
    return current_app.config["_models"].get("courseware")


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


@courseware_bp.route("/courseware/detect", methods=["POST"])
def courseware_detect():
    """
    POST /courseware/detect
    multipart/form-data: image=<图片文件>, [confidence=0.35]
    返回: {"boxes": [...], "summary": {"text_block_count": 3, ...}}
    """
    try:
        model = _get_model()
        if model is None:
            return jsonify({"error": "课件检测模型未加载"}), 503

        img = _read_image_from_request("image")
        if img is None:
            return jsonify({"error": "缺少图片文件（字段名: image）"}), 400

        confidence = float(request.form.get("confidence", 0.35))

        results = model(img, conf=confidence, verbose=False)

        boxes = []
        summary = {
            "text_block_count": 0,
            "table_count": 0,
            "diagram_count": 0,
            "formula_count": 0,
        }

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

                # 统计各类型数量
                if label == "text_block":
                    summary["text_block_count"] += 1
                elif label == "table":
                    summary["table_count"] += 1
                elif label == "diagram":
                    summary["diagram_count"] += 1
                elif label == "formula":
                    summary["formula_count"] += 1

        print(f"[courseware] 课件检测完成: {summary}")
        return jsonify({"boxes": boxes, "summary": summary})

    except Exception:
        print(f"[courseware] 课件检测异常:\n{traceback.format_exc()}")
        return jsonify({"error": "课件检测服务内部异常"}), 500
