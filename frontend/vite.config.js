import { fileURLToPath, URL } from 'node:url'
import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

const __dirname = fileURLToPath(new URL('.', import.meta.url))
const rootDir = path.resolve(__dirname, '..')

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, rootDir, 'VITE_')

  return {
    // 插件
    plugins: [vue()],

    // 路径别名
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },

    // echarts预构建 — 修复504
    optimizeDeps: {
      include: ['echarts']
    },

    // 从项目根目录读取 .env 文件
    envDir: rootDir,

    // 开发服务器
    server: {
      // 前端端口
      port: 5173,

      // 接口代理
      proxy: {
        '/api': {
          // SpringBoot 后端 — 通过环境变量 VITE_API_BASE_URL 配置
          target: env.VITE_API_BASE_URL || 'http://localhost:8081',

          // 允许跨域
          changeOrigin: true

          // 如果后端没有 /api 前缀：
          // rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    }
  }
})
