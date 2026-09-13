import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      manifest: {
        name: 'KAWA',
        short_name: 'KAWA',
        description: 'One identity for your loyalty cards',
        display: 'standalone',
        start_url: '/',
        theme_color: '#ffffff',
        background_color: '#ffffff',
         icons: [
          {
            src: '/icons/kawa-192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/icons/kawa-512.png',
            sizes: '512x512',
            type: 'image/png'
          },
          {
            src: '/icons/kawa-512-maskable.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable'
          }
        ]
      }
    })
  ]
})
