import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080',
        changeOrigin: true,
        // Browser Network tab shows :5173 (same-origin); this logs the real upstream.
        configure(proxy) {
          proxy.on('proxyReq', (_proxyReq, req) => {
            const target = process.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080'
            console.log(`[vite proxy] ${req.method} ${req.url} -> ${target}${req.url}`)
          })
        },
      },
    },
  },
})
