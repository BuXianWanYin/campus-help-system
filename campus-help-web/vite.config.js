import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  // 从环境变量获取配置
  const serverPort = parseInt(env.VITE_SERVER_PORT)
  const proxyTarget = env.VITE_SERVER_PROXY_TARGET
  const baseApi = env.VITE_APP_BASE_API

  return {
    plugins: [vue()],
    server: {
      port: serverPort,
      open: true, // 启动后自动打开浏览器
      proxy: {
        [baseApi]: {
          target: proxyTarget,
          changeOrigin: true,
          // 不重写路径，直接转发，因为后端 Controller 路径已经包含 /api
          // rewrite: (path) => path.replace(new RegExp(`^${baseApi}`), '')
        }
      }
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    }
  }
})

