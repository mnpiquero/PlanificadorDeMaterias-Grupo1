/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        uade: {
          primary: '#002B80',
          secondary: '#0040BF',
          light: '#F5F6FA',
          white: '#FFFFFF',
        },
      },
      fontFamily: {
        sans: ['Montserrat', 'Poppins', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

