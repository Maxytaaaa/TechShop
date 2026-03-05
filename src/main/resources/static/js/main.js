// ── HAMBURGER MENU ───────────────────────────────────────
const hamburger = document.querySelector('.hamburger');
const nav = document.querySelector('.nav');
if (hamburger && nav) {
    hamburger.addEventListener('click', () => {
        nav.classList.toggle('open');
        const spans = hamburger.querySelectorAll('span');
        spans[0].style.transform = nav.classList.contains('open') ? 'translateY(6.5px) rotate(45deg)' : '';
        spans[1].style.opacity = nav.classList.contains('open') ? '0' : '';
        spans[2].style.transform = nav.classList.contains('open') ? 'translateY(-6.5px) rotate(-45deg)' : '';
    });
}

// ── ACTIVE NAV LINK ──────────────────────────────────────
document.querySelectorAll('.nav__link').forEach(link => {
    try {
        const linkPath = new URL(link.href, location).pathname;
        const current = window.location.pathname;
        if (linkPath !== '/' && current.startsWith(linkPath)) {
            link.classList.add('active');
        } else if (linkPath === '/' && current === '/') {
            link.classList.add('active');
        }
    } catch (e) {
    }
});

// ── QTY BUTTONS (FIXED: works even if script loads before HTML) ─────────────
(function () {
    function clamp(n, min, max) {
        if (!Number.isFinite(n)) return min;
        if (Number.isFinite(max)) return Math.min(Math.max(n, min), max);
        return Math.max(n, min);
    }

    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.qty-btn');
        if (!btn) return;

        const targetSelector = btn.dataset.target;
        if (!targetSelector) return;

        const input = document.querySelector(targetSelector);
        if (!input) return;

        let val = parseInt(input.value, 10);
        const min = input.min !== '' ? parseInt(input.min, 10) : 1;
        const max = input.max !== '' ? parseInt(input.max, 10) : 99;

        if (!Number.isFinite(val)) val = min;

        if (btn.dataset.dir === 'up') val = clamp(val + 1, min, max);
        if (btn.dataset.dir === 'down') val = clamp(val - 1, min, max);

        input.value = val;

        // trigger events so any existing logic reacts same as spinner arrows
        input.dispatchEvent(new Event('input', {bubbles: true}));
        input.dispatchEvent(new Event('change', {bubbles: true}));
    });
})();

// ── LOADING STATE ON BUTTONS ─────────────────────────────
document.querySelectorAll('form:not(#checkout-form)').forEach(form => {
    form.addEventListener('submit', function () {
        const btn = this.querySelector('button[type="submit"]');
        if (!btn) return;

        if (btn.classList.contains('btn--danger')) return;

        const originalText = btn.textContent;
        btn.disabled = true;
        btn.style.opacity = '0.7';

        let dots = 0;
        const interval = setInterval(() => {
            dots = (dots + 1) % 4;
            btn.textContent = originalText.trim() + '.'.repeat(dots);
        }, 300);

        setTimeout(() => {
            clearInterval(interval);
            btn.disabled = false;
            btn.style.opacity = '';
            btn.textContent = originalText;
        }, 8000);
    });
});

// ── FADE-UP OBSERVER ─────────────────────────────────────
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
            observer.unobserve(entry.target);
        }
    });
}, {threshold: 0.08});

document.querySelectorAll('.product-card').forEach((el, i) => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = `opacity 0.4s ease ${i * 0.04}s, transform 0.4s ease ${i * 0.04}s, border-color 0.22s ease`;
    observer.observe(el);
});