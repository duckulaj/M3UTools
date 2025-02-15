
// src/main/resources/static/js/scrollToTop.js
document.addEventListener("DOMContentLoaded", function() {
    var returnToTopButton = document.getElementById("return-to-top");

    window.addEventListener("scroll", function() {
        if (window.scrollY > 200) {
            returnToTopButton.style.display = "block";
        } else {
            returnToTopButton.style.display = "none";
        }
    });

    returnToTopButton.addEventListener("click", function() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
});
