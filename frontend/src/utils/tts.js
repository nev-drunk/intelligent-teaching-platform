/**
 * TTS 语音工具 — 统一使用服务器 DashScope TTS
 * 不再使用浏览器 speechSynthesis
 */
import request from '@/api/request'

/**
 * 调用服务器 TTS 合成并返回音频 URL
 */
export async function synthesizeServerSpeech(text) {
  try {
    const res = await request.post('/api/tts/synthesize', { text })
    if (res.code === 200 && res.data?.audio_url) {
      return res.data.audio_url
    }
  } catch (e) {
    console.warn('Server TTS failed:', e)
  }
  return null
}

/**
 * 播放服务器 TTS 音频
 */
export function playAudioUrl(audioUrl) {
  if (!audioUrl) return
  const url = audioUrl.startsWith('http') ? audioUrl : 'http://localhost:8081/' + audioUrl
  const audio = new Audio(url)
  audio.play().catch(() => {})
  return audio
}

/**
 * 发布公告后自动播放
 */
export async function speakNotice(title, content) {
  const text = `${title}。${content}`
  const url = await synthesizeServerSpeech(text)
  if (url) playAudioUrl(url)
}

/**
 * 简单文本朗读（兼容旧代码）
 */
export async function speakText(text) {
  const url = await synthesizeServerSpeech(text)
  if (url) playAudioUrl(url)
}

/**
 * 登录成功播报
 */
export async function speakLoginSuccess(name) {
  await speakText(`${name}，登录成功，欢迎使用智能化在线教学支持服务平台。`)
}
