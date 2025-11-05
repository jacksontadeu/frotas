
  // Aplica tema salvo
  document.addEventListener("DOMContentLoaded", () => {
    const savedTheme = localStorage.getItem("theme") || "light-mode";
    document.body.classList.add(savedTheme);
  });

  // Alterna entre os temas
  function toggleTheme() {
    const body = document.body;
    if (body.classList.contains("light-mode")) {
      body.classList.replace("light-mode", "dark-mode");
      localStorage.setItem("theme", "dark-mode");
    } else {
      body.classList.replace("dark-mode", "light-mode");
      localStorage.setItem("theme", "light-mode");
    }
  }

