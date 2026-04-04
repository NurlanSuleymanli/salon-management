/**
 * shared.js — LuxCut Salon Management System
 * Common utilities: auth, toast, routing, API helper with auto token refresh
 */

const BASE = 'http://localhost:8080';

// ── TOKEN & SESSION STORAGE ──────────────────────────────
export function getToken()        { return localStorage.getItem('salon_token'); }
export function getRefreshToken() { return localStorage.getItem('salon_refresh'); }
export function getUser()         { return JSON.parse(localStorage.getItem('salon_user') || '{}'); }

export function saveTokens(accessToken, refreshToken) {
  localStorage.setItem('salon_token',   accessToken);
  if (refreshToken) localStorage.setItem('salon_refresh', refreshToken);
}

export function clearSession() {
  localStorage.clear();
}

export function authHeader() {
  return {
    'Authorization': 'Bearer ' + getToken(),
    'Content-Type':  'application/json'
  };
}

// ── JWT PARSER ───────────────────────────────────────────
export function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
  } catch { return {}; }
}

export function isTokenExpired(token) {
  if (!token) return true;
  const payload = parseJwt(token);
  if (!payload.exp) return true;
  // exp is in seconds, Date.now() in milliseconds
  return payload.exp * 1000 < Date.now();
}

// ── ROUTING ───────────────────────────────────────────────
// Role is embedded in the JWT `role` claim (e.g. "ADMIN", "BARBER", "CUSTOMER")
export function routeByRole(token) {
  const payload = parseJwt(token);
  // Backend puts role as plain string: "ADMIN", "BARBER", "CUSTOMER"
  const roleStr = (payload && payload.role) ? String(payload.role) : '';
  const role = roleStr.toUpperCase();
  if (role === 'ADMIN')  return 'admin.html';
  if (role === 'BARBER') return 'barber.html';
  return 'customer.html';
}

// ── GUARD ROUTE ───────────────────────────────────────────
// Call at top of every protected page. Returns false and redirects if not authenticated.
export function guardRoute() {
  const token = getToken();
  if (!token) {
    window.location.href = 'index.html';
    return false;
  }
  // If access token is expired, try to silently refresh before allowing page to load
  if (isTokenExpired(token)) {
    _silentRefreshAndReload();
    return false;
  }
  return true;
}

// Silently refresh the token. On success, reload. On failure, redirect to login.
async function _silentRefreshAndReload() {
  const refreshToken = getRefreshToken();
  if (!refreshToken || isTokenExpired(refreshToken)) {
    clearSession();
    window.location.href = 'index.html';
    return;
  }
  try {
    const res = await fetch(BASE + '/auth/refresh', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ refreshToken })
    });
    if (!res.ok) throw new Error('Refresh failed');
    const data = await res.json();
    saveTokens(data.accessToken, data.refreshToken);
    // Update stored user info in case role changed
    if (data.fullName || data.email) {
      const u = getUser();
      localStorage.setItem('salon_user', JSON.stringify({
        fullName: data.fullName || u.fullName,
        email:    data.email    || u.email,
        phone:    data.phone    || u.phone,
        role:     data.role     || u.role
      }));
    }
    window.location.reload();
  } catch {
    clearSession();
    window.location.href = 'index.html';
  }
}

// ── LOGOUT ────────────────────────────────────────────────
export async function doLogout() {
  if (!confirm('Çıxmaq istədiyinizə əminsiniz?')) return;
  try {
    await fetch(BASE + '/auth/logout', { method: 'POST', headers: authHeader() });
  } catch(e) { /* ignore network errors */ }
  clearSession();
  window.location.href = 'index.html';
}

// ── API HELPER with Auto Token Refresh ───────────────────
// Usage: api('/path', { method:'POST', body: JSON.stringify({}) })
// Set options.auth = false to skip Authorization header (public endpoints)
let _isRefreshing = false;
let _refreshQueue = [];

export async function api(path, options = {}) {
  const url = BASE + path;

  const makeHeaders = () =>
    options.auth !== false ? authHeader() : { 'Content-Type': 'application/json' };

  let res = await fetch(url, { ...options, headers: makeHeaders() });

  // If 401 Unauthorized, attempt one token refresh then retry
  if (res.status === 401 && options.auth !== false) {
    const refreshed = await _tryRefreshToken();
    if (refreshed) {
      // Retry original request with new token
      res = await fetch(url, { ...options, headers: makeHeaders() });
    } else {
      // Refresh failed → logout
      clearSession();
      window.location.href = 'index.html';
      throw new Error('Sessiya başa çatdı. Yenidən daxil olun.');
    }
  }

  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const d = await res.json();
      msg = d.message || d.error || msg;
    } catch(e) {}
    throw new Error(msg);
  }

  // 204 No Content
  if (res.status === 204) return null;
  return res.json();
}

// Refresh the access token. Returns true on success, false on failure.
async function _tryRefreshToken() {
  // If already refreshing, queue this request
  if (_isRefreshing) {
    return new Promise(resolve => { _refreshQueue.push(resolve); });
  }

  _isRefreshing = true;
  const refreshToken = getRefreshToken();

  if (!refreshToken || isTokenExpired(refreshToken)) {
    _isRefreshing = false;
    _refreshQueue.forEach(r => r(false));
    _refreshQueue = [];
    return false;
  }

  try {
    const res = await fetch(BASE + '/auth/refresh', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ refreshToken })
    });
    if (!res.ok) throw new Error('Refresh failed');
    const data = await res.json();
    saveTokens(data.accessToken, data.refreshToken);
    _isRefreshing = false;
    _refreshQueue.forEach(r => r(true));
    _refreshQueue = [];
    return true;
  } catch {
    _isRefreshing = false;
    _refreshQueue.forEach(r => r(false));
    _refreshQueue = [];
    return false;
  }
}

// ── TOAST ─────────────────────────────────────────────────
export function toast(title, msg, type = 'success') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const t = document.createElement('div');
  t.className = `toast ${type}`;
  t.innerHTML = `<span>${icons[type] || 'ℹ️'}</span>
    <div>
      <div class="toast-title">${title}</div>
      <div>${msg}</div>
    </div>`;
  container.appendChild(t);
  setTimeout(() => {
    t.style.opacity = '0';
    t.style.transform = 'translateX(30px)';
    t.style.transition = '.3s';
    setTimeout(() => t.remove(), 310);
  }, 4000);
}

// ── SECTION NAVIGATION ───────────────────────────────────
export function showSection(id, navTabs) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.getElementById('sec-' + id)?.classList.add('active');
  if (navTabs) {
    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    const idx = navTabs.indexOf(id);
    const tabs = document.querySelectorAll('.nav-tab');
    if (idx >= 0 && tabs[idx]) tabs[idx].classList.add('active');
  }
}

// ── LOADING HTML ─────────────────────────────────────────
export function loadingHTML() {
  return '<div class="loading-dots"><span></span><span></span><span></span></div>';
}

export function emptyHTML(icon, title, msg = '') {
  return `<div class="empty-state"><div class="empty-icon">${icon}</div><h3>${title}</h3><p>${msg}</p></div>`;
}

// ── STATUS MAPS ───────────────────────────────────────────
export const STATUS_AZ = {
  PENDING:   'Gözləmədə',
  CONFIRMED: 'Təsdiqlənib',
  COMPLETED: 'Tamamlandı',
  CANCELLED: 'Ləğv edilib'
};
export const STATUS_CLS = {
  PENDING:   'badge-pending',
  CONFIRMED: 'badge-confirmed',
  COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled'
};

export const DAY_AZ = {
  MONDAY:    'Bazar ertəsi',
  TUESDAY:   'Çərşənbə axşamı',
  WEDNESDAY: 'Çərşənbə',
  THURSDAY:  'Cümə axşamı',
  FRIDAY:    'Cümə',
  SATURDAY:  'Şənbə',
  SUNDAY:    'Bazar'
};

// ── PAGINATION BUILDER ───────────────────────────────────
export function buildPagination(elId, totalPages, current, onPageChange) {
  const el = document.getElementById(elId);
  if (!el || totalPages <= 1) { if (el) el.innerHTML = ''; return; }

  let html = `<button class="pg-btn" ${current === 0 ? 'disabled' : ''} data-page="${current - 1}">← Əvvəlki</button>`;
  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || Math.abs(i - current) <= 1)
      html += `<button class="pg-btn ${i === current ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
    else if (Math.abs(i - current) === 2)
      html += '<span style="padding:.4rem .3rem;color:var(--gray-400)">…</span>';
  }
  html += `<button class="pg-btn" ${current >= totalPages - 1 ? 'disabled' : ''} data-page="${current + 1}">Növbəti →</button>`;
  el.innerHTML = html;
  el.querySelectorAll('.pg-btn:not([disabled])').forEach(btn => {
    btn.addEventListener('click', () => onPageChange(parseInt(btn.dataset.page)));
  });
}

// ── FORMAT DATE ───────────────────────────────────────────
export function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('az-AZ', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}
