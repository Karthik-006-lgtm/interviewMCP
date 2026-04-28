export default {
    content: ["./index.html", "./src/**/*.{ts,tsx}"],
    theme: {
        extend: {
            colors: {
                ink: "#132238",
                sand: "#f6efe6",
                ember: "#d97706",
                mint: "#0f766e",
                plum: "#6b4eff"
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
};
