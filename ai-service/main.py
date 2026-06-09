"""
智能化在线教学支持服务平台 — AI 微服务入口
仅负责启动 Flask 应用，所有路由由 app 包注册
"""
from app import create_app

app = create_app()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=False)
