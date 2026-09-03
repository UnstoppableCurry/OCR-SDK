(function () {
  "use strict";

  var toggle = document.getElementById("nav-toggle");
  var nav = document.getElementById("sidebar-nav");
  var links = document.querySelectorAll("#sidebar-nav a[href^='#']");
  var sections = [];

  links.forEach(function (link) {
    var id = link.getAttribute("href").slice(1);
    var section = document.getElementById(id);
    if (section) {
      sections.push({ id: id, link: link, el: section });
    }
  });

  if (toggle && nav) {
    toggle.addEventListener("click", function () {
      var expanded = toggle.getAttribute("aria-expanded") === "true";
      toggle.setAttribute("aria-expanded", String(!expanded));
      nav.classList.toggle("is-open", !expanded);
    });

    links.forEach(function (link) {
      link.addEventListener("click", function () {
        toggle.setAttribute("aria-expanded", "false");
        nav.classList.remove("is-open");
      });
    });
  }

  if ("IntersectionObserver" in window && sections.length) {
    var observer = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            sections.forEach(function (item) {
              item.link.removeAttribute("aria-current");
            });
            var active = sections.find(function (item) {
              return item.id === entry.target.id;
            });
            if (active) {
              active.link.setAttribute("aria-current", "true");
            }
          }
        });
      },
      { rootMargin: "-40% 0px -50% 0px", threshold: 0 }
    );

    sections.forEach(function (item) {
      observer.observe(item.el);
    });
  }
})();
