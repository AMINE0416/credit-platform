import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    // Minification avec esbuild (plus rapide et inclus par défaut)
    minify: 'esbuild',
    // Code splitting optimisé pour de meilleures performances
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // Séparer React et React Router
            if (id.includes('react') || id.includes('react-dom') || id.includes('react-router')) {
              return 'vendor-react';
            }
            // Séparer les icônes
            if (id.includes('lucide-react')) {
              return 'vendor-icons';
            }
            // Séparer axios
            if (id.includes('axios')) {
              return 'vendor-utils';
            }
          }
        },
      },
    },
    chunkSizeWarningLimit: 1000,
    reportCompressedSize: true,
    sourcemap: false,
  },
  server: {
    port: 5173,
  },
  optimizeDeps: {
    include: ['react', 'react-dom', 'react-router-dom', 'axios', 'lucide-react'],
  },
});
