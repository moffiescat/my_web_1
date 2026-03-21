const btn = document.getElementById('btn');
btn.addEventListener('click', () => {
    alert('IDEA开发的网页部署成功啦～');
});

function displayDateTime() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const day = now.getDate();
    const dateTimeElement = document.getElementById('date-time');
    dateTimeElement.textContent = `当前日期：${year}年${month}月${day}日`;
}

async function loadProfile() {
    try {
        const response = await fetch('/api/profile');
        const profile = await response.json();
        
        document.getElementById('name').textContent = `姓名：${profile.name}`;
        document.getElementById('age').textContent = `年龄：${profile.age}`;
        document.getElementById('profession').textContent = `职业：${profile.profession}`;
        document.getElementById('email').textContent = `邮箱：${profile.email}`;
    } catch (error) {
        console.error('获取个人信息失败:', error);
    }
}

function updateAuthUI() {
    const username = localStorage.getItem('username');
    const email = localStorage.getItem('email');
    const userInfo = document.getElementById('userInfo');
    const authButtons = document.getElementById('authButtons');
    
    if (username) {
        userInfo.textContent = `欢迎，${username} (${email})`;
        authButtons.innerHTML = '<button class="logout-btn" onclick="logout()">退出登录</button>';
    } else {
        userInfo.textContent = '';
        authButtons.innerHTML = '<a href="login.html" class="login-link">登录</a>';
    }
}

function logout() {
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    updateAuthUI();
    window.location.href = 'login.html';
}

// 检查登录状态
function checkLogin() {
    const username = localStorage.getItem('username');
    if (!username) {
        window.location.href = 'login.html';
    }
}

// 首页需要登录才能访问
if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
    checkLogin();
}

displayDateTime();
loadProfile();
updateAuthUI();
