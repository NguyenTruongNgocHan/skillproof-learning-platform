(function () {
  try {
    var pref = localStorage.getItem("skillproof-theme");
    var system = matchMedia("(prefers-color-scheme: dark)").matches
      ? "dark"
      : "light";
    var resolved = pref === "light" || pref === "dark" ? pref : system;
    document.documentElement.dataset.theme = resolved;
    document.documentElement.style.colorScheme = resolved;
  } catch (_) {
    document.documentElement.dataset.theme = "light";
  }
})();
