import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  root: './',
  build: {
    outDir: '../../resources/META-INF/views',
    emptyOutDir: false,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'src/main.js'),
      },
      output: {
        assetFileNames: (assetInfo) => {
          // Place CSS in css/ directory
          if (assetInfo.name.endsWith('.css')) {
            return 'css/[name][extname]';
          }
          // Webfonts
          if (assetInfo.name.match(/\.(woff2?|eot|ttf|otf)$/)) {
            return 'fonts/[name][extname]';
          }
          // Everything else in root
          return '[name][extname]';
        },
        entryFileNames: 'js/[name].js',
        chunkFileNames: 'js/[name]-[hash].js',
      },
    },
    // Don't split CSS
    cssCodeSplit: false,
    // Generate source maps for debugging
    sourcemap: true,
    // Minify for production
    minify: 'esbuild',
  },
  css: {
    preprocessorOptions: {
      scss: {
        // Use modern Sass API
        api: 'modern-compiler',
      },
    },
  },
  server: {
    port: 3000,
    proxy: {
      // Proxy API requests to Spring Boot during development
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
    },
  },
});
