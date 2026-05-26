const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

if (new URLSearchParams(window.location.search).get('mode') === 'register') {
    container.classList.add('active');
}

registerBtn?.addEventListener('click', () => {
    container.classList.add('active');
});

loginBtn?.addEventListener('click', () => {
    container.classList.remove('active');
});
