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

// Dark Mode Toggle
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme') || 'light';
  const htmlElement = document.documentElement;
  const themeIcon = document.getElementById('theme-icon');

  const setTheme = (theme) => {
    htmlElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('theme', theme);

    if (themeIcon) {
      if (theme === 'dark') {
        themeIcon.classList.remove('fa-moon');
        themeIcon.classList.add('fa-sun');
      } else {
        themeIcon.classList.remove('fa-sun');
        themeIcon.classList.add('fa-moon');
      }
    }
  };

  // Set initial theme
  setTheme(savedTheme);

  // Add click handler for theme toggle
  const themeToggle = document.getElementById('theme-toggle');
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      const currentTheme = htmlElement.getAttribute('data-bs-theme');
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
      setTheme(newTheme);
    });
  }
};

// Initialize theme when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initTheme);
} else {
  initTheme();
}

console.log('Source Code Portal - Assets loaded');
