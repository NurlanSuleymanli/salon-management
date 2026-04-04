/**
 * index.js — Login / Register page logic
 * Handles login, register, tab switching and session management.
 */
import { toast, routeByRole, saveTokens, getToken, isTokenExpired } from './shared.js';

const BASE = 'http://localhost:8080';

// ── Tab switching ─────────────────────────────────────────
export function switchTab(tab) {
  document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.form-section').forEach(s => s.classList.remove('active'));
  document.getElementById('tab-' + tab).classList.add('active');
  document.getElementById('section-' + tab).classList.add('active');
  hideErr();
}

function showErr(msg) {
  const el = document.getElementById('err-box');
  el.textContent = msg;
  el.style.display = 'block';
}
function hideErr() {
  const el = document.getElementById('err-box');
  if (el) el.style.display = 'none';
}

function setLoading(btnId, on) {
  const btn = document.getElementById(btnId);
  if (!btn) return;
  btn.classList.toggle('loading', on);
  btn.disabled = on;
}

/**
 * Save full session info from AuthResponse.
 * Stores accessToken, refreshToken and user profile.
 */
function saveSession(data) {
  saveTokens(data.accessToken, data.refreshToken);
  localStorage.setItem('salon_user', JSON.stringify({
    id:       data.id,
    fullName: data.fullName,
    email:    data.email,
    phone:    data.phone,
    role:     data.role
  }));
}

/**
 * Route user to correct dashboard after login/register.
 * Uses data.role from the AuthResponse (most reliable).
 * Falls back to parsing the JWT if role is missing from response.
 */
function navigateToHome(data) {
  const roleStr = (data && data.role) ? String(data.role) : '';
  const role = roleStr.toUpperCase();
  if (role === 'ADMIN')  return (window.location.href = 'admin.html');
  if (role === 'BARBER') return (window.location.href = 'barber.html');
  // Fallback: parse JWT for role claim
  if (data && data.accessToken) return (window.location.href = routeByRole(data.accessToken));
  window.location.href = 'customer.html';
}

// ── LOGIN ─────────────────────────────────────────────────
export async function doLogin() {
  hideErr();
  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-pass').value;
  if (!email || !password) return showErr('E-poçt və şifrəni daxil edin.');
  setLoading('btn-login', true);
  try {
    const res = await fetch(BASE + '/auth/login', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Giriş xətası!');
    saveSession(data);
    navigateToHome(data);
  } catch(e) {
    showErr(e.message);
  } finally {
    setLoading('btn-login', false);
  }
}

// ── REGISTER ──────────────────────────────────────────────
export async function doRegister() {
  hideErr();
  const fullName = document.getElementById('reg-name').value.trim();
  const email    = document.getElementById('reg-email').value.trim();
  const phone    = document.getElementById('reg-phone').value.trim();
  const password = document.getElementById('reg-pass').value;
  if (!fullName || !email || !phone || !password) return showErr('Bütün xanaları doldurun.');
  if (password.length < 8) return showErr('Şifrə ən az 8 simvol olmalıdır.');
  setLoading('btn-register', true);
  try {
    const res = await fetch(BASE + '/auth/register', {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ fullName, email, phone, password })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Qeydiyyat xətası!');
    saveSession(data);
    navigateToHome(data);
  } catch(e) {
    showErr(e.message);
  } finally {
    setLoading('btn-register', false);
  }
}

// ── AUTO REDIRECT if already logged in ───────────────────
(function init() {
  const token = getToken();
  if (token && !isTokenExpired(token)) {
    // Already logged in with valid token → redirect to dashboard
    window.location.href = routeByRole(token);
    return;
  }

  // Try silent refresh if access token expired but refresh token exists
  const refreshToken = localStorage.getItem('salon_refresh');
  if (refreshToken && token && isTokenExpired(token)) {
    // Attempt refresh, then redirect
    (async () => {
      try {
        const res = await fetch(BASE + '/auth/refresh', {
          method:  'POST',
          headers: { 'Content-Type': 'application/json' },
          body:    JSON.stringify({ refreshToken })
        });
        if (!res.ok) throw new Error('Refresh failed');
        const data = await res.json();
        saveSession(data);
        navigateToHome(data);
      } catch {
        localStorage.clear(); // Refresh failed, clear stale data
      }
    })();
    return;
  }

  // Expose to global scope for inline onclick handlers
  window.switchTab  = switchTab;
  window.doLogin    = doLogin;
  window.doRegister = doRegister;

  // Enter key shortcuts
  document.getElementById('login-pass')?.addEventListener('keydown', e => {
    if (e.key === 'Enter') doLogin();
  });
  document.getElementById('reg-pass')?.addEventListener('keydown', e => {
    if (e.key === 'Enter') doRegister();
  });
})();
