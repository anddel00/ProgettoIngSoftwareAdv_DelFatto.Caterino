import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // AGGIUNGI QUESTO BLOCCO:
  define: {
    global: 'window',
  }
})