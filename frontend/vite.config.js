import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  // 1. 插件配置
  plugins: [vue()],

  // 2. 路径别名配置：允许在代码中使用 @/ 代替 ./src/
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  // 3. 开发服务器与跨域代理配置
  server: {
    port: 5173, // 前端本地启动的端口号
    proxy: {
      // 匹配所有以 /api 开头的请求路径
      '/api': {
        target: 'http://localhost:8081', // 你的 Spring Boot 后端服务地址
        changeOrigin: true // 允许跨域欺骗（修改请求头中的 Host 为目标 URL）
        // rewrite: (path) => path.replace(/^\/api/, '')
        // ^ 如果你的后端接口本身就带有 /api（如 /api/auth/login），则不需要上面这行 rewrite。
        // ^ 如果后端接口没有 /api（如直接是 /auth/login），请取消上面这行的注释，用于重写去掉 /api 前缀。
      }
    }
  }
})
