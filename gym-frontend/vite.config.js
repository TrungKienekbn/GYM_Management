import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] })
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) }
  },
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        // Dùng IPv4 cụ thể để Node không thử đồng thời ::1 và 127.0.0.1 trên
        // Windows (có thể gây AggregateError/EACCES không ổn định).
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        timeout: 30000,
        proxyTimeout: 30000
      }
    }
  }
})
