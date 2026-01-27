// Main entry point for Vite build
// Import Bootstrap 5 and custom SCSS
import '../scss/app.scss';

// Import Bootstrap JS (includes Popper.js)
import * as bootstrap from 'bootstrap';

// Import HTMX
import htmx from 'htmx.org';

// Make htmx available globally
window.htmx = htmx;

// Make Bootstrap available globally (for legacy code if needed)
window.bootstrap = bootstrap;

console.log('Source Code Portal - Assets loaded');
