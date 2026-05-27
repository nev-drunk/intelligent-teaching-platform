/**
 * 💥 TTS 语音通知算法（浏览器原生 Web Speech API）
 * 发公告或登录成功时，将文本自动朗读。
 */
export function speakText(text, options = {}) {
  if (!text || typeof window === 'undefined' || !window.speechSynthesis) {
    return
  }
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(String(text))
  utterance.lang = options.lang || 'zh-CN'
  utterance.rate = options.rate ?? 1
  utterance.pitch = options.pitch ?? 1
  utterance.volume = options.volume ?? 1
  window.speechSynthesis.speak(utterance)
}

export function speakNotice(title, content) {
  speakText(`${title}。${content}`)
}

export function speakLoginSuccess(name) {
  speakText(`${name}，登录成功，欢迎使用智能化在线教学支持服务平台。`)
}
