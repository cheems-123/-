/* 智记 MindNotes 公共脚本：请求封装、登录态、导航、工具函数、片段渲染 */
window.APP = { me: null };

async function api(path, options = {}) {
    const anonymous = !!options.anonymous;   // 该请求允许未登录（如 /api/auth/me）
    const opt = Object.assign({ credentials: 'same-origin', headers: {} }, options);
    delete opt.anonymous;
    if (opt.body && typeof opt.body !== 'string' && !(opt.body instanceof FormData)) {
        opt.headers['Content-Type'] = 'application/json';
        opt.body = JSON.stringify(opt.body);
    }
    let res;
    try {
        res = await fetch(path, opt);
    } catch (e) {
        // 请求根本没到达后端：多为双击 html 文件或用 IDEA 内置预览（端口 63342）打开
        showServerTip();
        throw new Error('无法连接后端服务，请通过 http://localhost:8080/index.html 访问页面');
    }
    const data = await res.json().catch(() => ({ code: 500, message: '响应解析失败' }));
    if (data.code === 401) {
        const page = location.pathname.split('/').pop();
        if (!anonymous && page !== 'login.html' && page !== 'register.html') {
            location.href = 'login.html';
        }
        throw new Error(data.message || '请先登录');
    }
    if (data.code !== 0) throw new Error(data.message || '请求失败');
    return data.data;
}
const apiGet = p => api(p, { method: 'GET' });
const apiPost = (p, body) => api(p, { method: 'POST', body });
const apiPut = (p, body) => api(p, { method: 'PUT', body });
const apiDel = p => api(p, { method: 'DELETE' });

async function ensureMe() {
    try { APP.me = await api('/api/auth/me', { method: 'GET', anonymous: true }); }
    catch { APP.me = null; }
    return APP.me;
}
function requireLogin() {
    if (!APP.me) { location.href = 'login.html'; return false; }
    return true;
}

function esc(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

function fmtTime(t) {
    if (!t) return '';
    return String(t).replace('T', ' ').substring(0, 16);
}

function toast(msg) {
    let el = document.getElementById('toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast';
        document.body.appendChild(el);
    }
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(el._t);
    el._t = setTimeout(() => el.classList.remove('show'), 1800);
}

async function uploadMedia(file, kind) {
    const fd = new FormData();
    fd.append('file', file);
    const res = await fetch('/api/files/upload?kind=' + kind, {
        method: 'POST', credentials: 'same-origin', body: fd
    });
    const j = await res.json();
    if (j.code !== 0) throw new Error(j.message);
    return j.data.url;
}

function getQuery(name) {
    return new URLSearchParams(location.search).get(name);
}

/* 导航渲染 */
function renderNav() {
    const nav = document.getElementById('siteNav');
    if (!nav) return;
    const me = APP.me;
    const right = me
        ? `<span class="nav-user">${esc(me.nickname || me.username)}　</span>
           <a class="nav-link" href="myNotes.html">我的笔记</a>
           <button class="btn ghost small" id="btnLogout">退出</button>`
        : `<a class="nav-link" href="login.html">登录</a>
           <a class="btn small" href="register.html">注册</a>`;
    nav.innerHTML = `
      <div class="nav-inner">
        <a class="brand" href="index.html"><span class="brand-logo">记</span><span>智记 MindNotes</span></a>
        <nav class="nav-links">
          <a class="nav-link" href="index.html">首页推荐</a>
          <a class="nav-link" href="publish.html">发布笔记</a>
          <a class="nav-link" href="myNotes.html">我的笔记</a>
        </nav>
        <div class="nav-right">${right}</div>
      </div>`;
    const btn = document.getElementById('btnLogout');
    if (btn) btn.onclick = async () => {
        await apiPost('/api/auth/logout');
        APP.me = null;
        location.href = 'index.html';
    };
}

/* 笔记片段统一渲染：文字 / 图片 / 音频 */
function renderSegments(segments) {
    if (!segments || !segments.length) return '<p class="muted">（该笔记暂无正文片段）</p>';
    return segments.map(seg => {
        if (seg.segType === 'IMAGE') {
            if (seg.mediaUrl) {
                return `<figure class="seg-image">
                    <img src="${esc(seg.mediaUrl)}" alt="${esc(seg.caption || '笔记图片')}" loading="lazy">
                    ${seg.caption ? `<figcaption>${esc(seg.caption)}</figcaption>` : ''}
                </figure>`;
            }
            return `<div class="media-empty">图片片段：${esc(seg.caption || '暂无图片文件')}</div>`;
        }
        if (seg.segType === 'AUDIO') {
            if (seg.mediaUrl) {
                return `<div class="seg-audio">
                    <audio controls src="${esc(seg.mediaUrl)}"></audio>
                    ${seg.caption ? `<div class="caption">${esc(seg.caption)}</div>` : ''}
                </div>`;
            }
            return `<div class="media-empty audio-empty">音频片段：${esc(seg.caption || '暂无音频文件')}</div>`;
        }
        return `<p class="seg-text">${esc(seg.contentText || '')}</p>`;
    }).join('');
}

/* 页面打开方式不正确（file:// 或 IDEA 内置预览）时的顶部提示条，只显示一次 */
function showServerTip() {
    if (document.getElementById('serverTip')) return;
    const bar = document.createElement('div');
    bar.id = 'serverTip';
    bar.style.cssText = 'position:fixed;left:0;right:0;top:0;z-index:9999;background:#b3261e;color:#fff;'
        + 'padding:10px 16px;font-size:13px;line-height:1.6;text-align:center;box-shadow:0 2px 8px rgba(0,0,0,.2)';
    bar.innerHTML = '无法连接后端服务：当前页面不是由 Spring Boot 提供（可能双击了 html 文件，或用 IDEA 内置预览在 63342 端口打开）。<br>'
        + '请先运行启动类 ZhMindNotesApplication，再在浏览器中访问 '
        + '<b>http://localhost:8080/index.html</b>';
    document.body.appendChild(bar);
}

/* 页面启动：先取登录态、渲染导航，再执行业务初始化（失败时给出可见提示，避免白屏） */
async function boot(initFn) {
    await ensureMe();
    renderNav();
    if (typeof initFn === 'function') {
        try { await initFn(); }
        catch (e) { toast(e && e.message ? e.message : '数据加载失败'); }
    }
}
