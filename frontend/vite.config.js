import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
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

  // 开发服务器
  server: {
    // 前端端口
    port: 5173,

    // 接口代理
    proxy: {
      '/api': {
        // SpringBoot 后端
        target: 'http://localhost:8081',

        // 允许跨域
        changeOrigin: true

        // 如果后端没有 /api 前缀：
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
