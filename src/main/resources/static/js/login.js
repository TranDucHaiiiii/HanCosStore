const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

if (container && new URLSearchParams(window.location.search).get('mode') === 'register') {
    container.classList.add('active');
}

registerBtn?.addEventListener('click', () => {
    container?.classList.add('active');
});

loginBtn?.addEventListener('click', () => {
    container?.classList.remove('active');
});

document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', () => {
        if (!form.checkValidity()) {
            return;
        }

        const submitButton = form.querySelector('button[type="submit"]');
        if (submitButton) {
            submitButton.disabled = true;
        }
    });
});
