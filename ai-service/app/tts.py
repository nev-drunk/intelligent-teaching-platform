"""
TTS 语音合成 Blueprint
POST /tts/synthesize — 文本转语音，使用阿里云 DashScope qwen3-tts-flash
"""
import os
import uuid
import traceback
import requests
import dashscope

from flask import Blueprint, request, jsonify, current_app, make_response

tts_bp = Blueprint("tts", __name__)

DEFAULT_VOICE = "Cherry"  # DashScope 默认女声


def _get_api_key():
    return current_app.config.get("DASHSCOPE_API_KEY", "")


@tts_bp.route("/tts/synthesize", methods=["POST"])
def tts_synthesize():
    """
    POST /tts/synthesize
    JSON: {"text": "内容", "voice": "Cherry"}
    返回: {"audio_url": "uploads/tts/xxxx.wav"}
    """
    try:
        data = request.get_json(force=True)
        text = data.get("text", "").strip()
        voice = data.get("voice", DEFAULT_VOICE)

        if not text:
            return jsonify({"error": "缺少 text 参数"}), 400

        api_key = _get_api_key()
        if not api_key:
            return jsonify({"error": "DashScope API Key 未配置"}), 500

        dashscope.api_key = api_key
        dashscope.base_http_api_url = 'https://dashscope.aliyuncs.com/api/v1'

        # 调用 DashScope TTS
        response = dashscope.MultiModalConversation.call(
            model="qwen3-tts-flash",
            text=text,
            voice=voice,
            language_type="Chinese"
        )

        if response.status_code != 200:
            print(f"[TTS] API 调用失败: {response.status_code} {response.message}")
            return jsonify({"error": f"TTS API 失败: {response.message}"}), 500

        # 下载音频文件
        audio_url_remote = response.output.audio.url
        audio_resp = requests.get(audio_url_remote, timeout=30)
        audio_resp.raise_for_status()

        # 保存到本地
        tts_dir = current_app.config["TTS_DIR"]
        os.makedirs(tts_dir, exist_ok=True)

        filename = f"{uuid.uuid4().hex}.wav"
        filepath = os.path.join(tts_dir, filename)
        with open(filepath, 'wb') as f:
            f.write(audio_resp.content)

        audio_url = f"uploads/tts/{filename}"
        print(f"[TTS] DashScope 合成完成: {len(text)} 字符 → {filepath}")
        return jsonify({"audio_url": audio_url})

    except Exception as ex:
        print(f"[TTS] 合成异常:\n{traceback.format_exc()}")
        return jsonify({"error": str(ex)}), 500


@tts_bp.route("/audio/<filename>", methods=["GET"])
def serve_audio(filename):
    """提供音频文件访问"""
    tts_dir = current_app.config["TTS_DIR"]
    file_path = os.path.join(tts_dir, filename)
    if not os.path.exists(file_path):
        return jsonify({"error": "音频文件不存在"}), 404
    with open(file_path, 'rb') as f:
        audio_data = f.read()
    resp = make_response(audio_data)
    resp.headers.set('Content-Type', 'audio/wav')
    return resp
