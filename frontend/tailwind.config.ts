import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#16081F",
        sand: "#EEF2FF",
        ember: "#1ED6FF",
        mint: "#7DEBFF",
        plum: "#FF4BC7",
        gold: "#FFC857",
        charcoal: "#212222",
        authGold: "#FEC51E",
        authCharcoal: "#212222"
      },
      fontFamily: {
        display: ["'Space Grotesk'", "sans-serif"],
        body: ["'DM Sans'", "sans-serif"]
      },
      boxShadow: {
        panel: "0 24px 70px rgba(19, 34, 56, 0.12)"
      }
    }
  },
  plugins: []
} satisfies Config;

