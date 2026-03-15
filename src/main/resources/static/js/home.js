// Scroll animation
const fades = document.querySelectorAll('.fade');

window.addEventListener('scroll', () => {
    fades.forEach(el => {
        const position = el.getBoundingClientRect().top;
        const screen = window.innerHeight;

        if (position < screen - 100) {
            el.classList.add('show');
        }
    });
});
