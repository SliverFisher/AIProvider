import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    proxy: {
      '/api': { target: 'https://msg-drag-chargers-twist.trycloudflare.com', changeOrigin: true, secure: true },
      '/ws': { target: 'wss://msg-drag-chargers-twist.trycloudflare.com', ws: true, changeOrigin: true },
    },
  },
})
