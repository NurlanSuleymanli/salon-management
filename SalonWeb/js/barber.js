/**
 * barber.js — Barber Panel Logic
 * Dashboard, Schedule, Working Hours, Profile
 */
import {
  api, toast, doLogout, guardRoute, getUser, showSection,
  loadingHTML, emptyHTML, STATUS_AZ, STATUS_CLS, DAY_AZ, formatDate, saveTokens
} from './shared.js';

const NAV = ['dashboard', 'schedule', 'hours', 'services', 'profile'];
let allReservations = [];
let editingWhId = null;

// ── INIT ─────────────────────────────────────────────────
(function init() {
  if (!guardRoute()) return;
  const u    = getUser();
  const init = (u.fullName || 'B')[0].toUpperCase();

  document.getElementById('hdr-avatar').textContent   = init;
  document.getElementById('hdr-name').textContent     = u.fullName || u.email || '';
  document.getElementById('dash-name').textContent    = u.fullName || 'Bərbər';
  document.getElementById('prof-avatar').textContent  = init;
  document.getElementById('prof-name').textContent    = u.fullName || '';
  document.getElementById('prof-email').textContent   = u.email || '';
  if (u.fullName) document.getElementById('upd-name').value  = u.fullName;
  if (u.phone)    document.getElementById('upd-phone').value = u.phone;

  // Expose globals
  window.doLogout        = doLogout;
  window.showSection     = _showSection;
  window.updateStatus    = updateStatus;
  window.openWhModal     = openWhModal;
  window.closeWhModal    = closeWhModal;
  window.saveWorkingHour = saveWorkingHour;
  window.updateProfile   = updateProfile;
  window.changePassword  = changePassword;
  window.openSvcModal    = openSvcModal;
  window.closeSvcModal   = closeSvcModal;
  window.saveSvc         = saveSvc;
  window.deleteSvc       = deleteSvc;

  loadDashboard();
})();

function _showSection(id) {
  showSection(id, NAV);
  if (id === 'schedule') renderScheduleTable(allReservations);
  if (id === 'hours')    loadWorkingHours();
  if (id === 'services') loadMyServices();
}

// ── MY SERVICES (BARBER CRUD) ───────────────────────────────
let barberProfile = null;
let editingSvcId  = null;

async function loadMyServices() {
  const el = document.getElementById('my-svc-grid');
  el.innerHTML = loadingHTML();
  try {
    // 1) get my barber profile to learn salonId
    barberProfile = await api('/api/barbers/me');

    // 2) get services that belong to MY salon (only active ones)
    const svcs = await api(`/api/services/barber/my-services`);

    if (!svcs.length) {
      el.innerHTML = emptyHTML('💈', 'Xidmət yoxdur', 'Xidmət Əlavə Et düyməsi ilə əlavə edin.');
      return;
    }

    el.innerHTML = svcs.map(s => `
      <div class="my-svc-card">
        <div class="my-svc-top">
          <div class="my-svc-name">${s.name}</div>
          <div class="my-svc-meta">
            <span class="my-svc-chip">&#9201; ${s.durationMin} dəq</span>
            <span class="my-svc-chip my-svc-price">${Number(s.price).toFixed(2)} ₼</span>
          </div>
        </div>
        <div class="my-svc-actions">
          <button class="btn-edit-wh" onclick='openSvcModal(${JSON.stringify(s)})'>Düzəlt</button>
          <button class="btn-del-svc"  onclick="deleteSvc(${s.id})">Sil</button>
        </div>
      </div>`).join('');

  } catch(e) {
    el.innerHTML = emptyHTML('⚠️', 'Xəta', e.message);
  }
}

function openSvcModal(svc) {
  editingSvcId = null;
  document.getElementById('svc-modal-title').textContent = svc ? 'Xidməti Düzəlt' : 'Xidmət Əlavə Et';
  if (svc) {
    editingSvcId = svc.id;
    document.getElementById('svc-name').value     = svc.name     || '';
    document.getElementById('svc-duration').value = svc.durationMin || '';
    document.getElementById('svc-price').value    = svc.price    || '';
  } else {
    document.getElementById('svc-name').value     = '';
    document.getElementById('svc-duration').value = '';
    document.getElementById('svc-price').value    = '';
  }
  document.getElementById('svc-modal').classList.remove('hidden');
}

function closeSvcModal() {
  document.getElementById('svc-modal').classList.add('hidden');
}

async function saveSvc() {
  const name        = document.getElementById('svc-name').value.trim();
  const durationMin = parseInt(document.getElementById('svc-duration').value);
  const price       = parseFloat(document.getElementById('svc-price').value);

  if (!name)               return toast('Xəta', 'Xidmət adı daxil edin.', 'error');
  if (!durationMin || durationMin < 5) return toast('Xəta', 'Müddəti düzgün daxil edin.', 'error');
  if (isNaN(price) || price < 0)       return toast('Xəta', 'Qiyməti düzgün daxil edin.', 'error');
  if (!barberProfile)      return toast('Xəta', 'Bərbər profili tapılmadı.', 'error');

  const body = { name, durationMin, price, salonId: barberProfile.salonId };

  try {
    if (editingSvcId) {
      await api(`/api/services/${editingSvcId}/update`, { method: 'PUT', body: JSON.stringify(body) });
      toast('Uğurlu!', 'Xidmət yeniləndi.');
    } else {
      await api('/api/services/add', { method: 'POST', body: JSON.stringify(body) });
      toast('Uğurlu!', 'Xidmət əlavə edildi.');
    }
    closeSvcModal();
    await loadMyServices();
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function deleteSvc(id) {
  if (!confirm('Bu xidməti silmək istəyirsiniz?')) return;
  try {
    await api(`/api/services/${id}/delete`, { method: 'DELETE' });
    toast('Uğurlu!', 'Xidmət silindi.');
    await loadMyServices();
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── DASHBOARD ────────────────────────────────────────────
async function loadDashboard() {
  try {
    allReservations = await api('/api/reservations/barber-schedule');

    document.getElementById('stat-total').textContent     = allReservations.length;
    document.getElementById('stat-pending').textContent   = allReservations.filter(r => r.status === 'PENDING').length;
    document.getElementById('stat-confirmed').textContent = allReservations.filter(r => r.status === 'CONFIRMED').length;
    document.getElementById('stat-completed').textContent = allReservations.filter(r => r.status === 'COMPLETED').length;

    renderRecentTable(allReservations.slice(0, 5));
  } catch(e) {
    document.getElementById('dash-recent').innerHTML = emptyHTML('⚠️', 'Xəta', e.message);
  }
}

function resRowHTML(r, mini = false) {
  const st         = r.status || 'PENDING';
  const canConfirm = st === 'PENDING';
  const canComplete = st === 'CONFIRMED';
  const dt = formatDate(r.startAt);
  return `<tr>
    <td><strong>${r.customerName || 'Müştəri'}</strong></td>
    <td>${(r.serviceNames || []).join(', ') || '—'}</td>
    <td>${dt}</td>
    ${mini ? '' : `<td><strong>${r.totalPrice || 0} ₼</strong></td>`}
    <td><span class="status-badge ${STATUS_CLS[st] || 'badge-pending'}">${STATUS_AZ[st] || st}</span></td>
    <td>
      ${canConfirm  ? `<button class="btn-status btn-confirm-it" onclick="updateStatus(${r.id},'CONFIRMED')">Təsdiqlə</button>` : ''}
      ${canComplete ? `<button class="btn-status btn-complete-it" onclick="updateStatus(${r.id},'COMPLETED')">Tamamla</button>` : ''}
    </td>
  </tr>`;
}

function renderRecentTable(list) {
  const el = document.getElementById('dash-recent');
  if (!list.length) { el.innerHTML = emptyHTML('📋', 'Rezervasiya yoxdur'); return; }
  el.innerHTML = `
    <table class="res-table">
      <thead><tr><th>Müştəri</th><th>Xidmət</th><th>Vaxt</th><th>Status</th><th>Əməliyyat</th></tr></thead>
      <tbody>${list.map(r => resRowHTML(r, true)).join('')}</tbody>
    </table>`;
}

function renderScheduleTable(list) {
  const el = document.getElementById('schedule-body');
  if (!list.length) { el.innerHTML = emptyHTML('📋', 'Rezervasiya yoxdur'); return; }
  el.innerHTML = `
    <table class="res-table">
      <thead><tr><th>Müştəri</th><th>Xidmətlər</th><th>Vaxt</th><th>Məbləğ</th><th>Status</th><th>Əməliyyat</th></tr></thead>
      <tbody>${list.map(r => resRowHTML(r, false)).join('')}</tbody>
    </table>`;
}

async function updateStatus(id, status) {
  try {
    await api(`/api/reservations/${id}/status`, {
      method: 'PUT',
      body:   JSON.stringify({ status })
    });
    toast('Uğurlu!', `Status "${STATUS_AZ[status]}" olaraq dəyişdirildi.`);
    await loadDashboard();
    renderScheduleTable(allReservations);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── WORKING HOURS ─────────────────────────────────────────
async function loadWorkingHours() {
  const el = document.getElementById('wh-grid');
  el.innerHTML = loadingHTML();
  try {
    const whList = await api('/api/working-hours/my-schedule');
    if (!whList.length) {
      el.innerHTML = emptyHTML('⏰', 'İş saatı əlavə edilməyib', 'Aşağıdakı düymə ilə əlavə edin.');
      return;
    }
    el.innerHTML = whList.map(wh => `
      <div class="wh-card">
        <div class="wh-day">${DAY_AZ[wh.dayOfWeek] || wh.dayOfWeek}</div>
        <div class="wh-time">${wh.startTime || '?'} – ${wh.endTime || '?'}</div>
        <div class="wh-actions">
          <button class="btn-edit-wh" onclick='openWhModal(${JSON.stringify(wh)})'>Düzəlt</button>
        </div>
      </div>`).join('');
  } catch(e) {
    el.innerHTML = emptyHTML('⚠️', 'Xəta', e.message);
  }
}

function openWhModal(wh) {
  editingWhId = null;
  document.getElementById('wh-modal-title').textContent = wh ? 'İş Saatını Düzəlt' : 'İş Saatı Əlavə Et';
  if (wh) {
    editingWhId = wh.id;
    document.getElementById('wh-day').value   = wh.dayOfWeek || 'MONDAY';
    document.getElementById('wh-start').value = wh.startTime || '09:00';
    document.getElementById('wh-end').value   = wh.endTime   || '18:00';
  } else {
    document.getElementById('wh-day').value   = 'MONDAY';
    document.getElementById('wh-start').value = '09:00';
    document.getElementById('wh-end').value   = '18:00';
  }
  document.getElementById('wh-modal').classList.remove('hidden');
}

function closeWhModal() {
  document.getElementById('wh-modal').classList.add('hidden');
}

async function saveWorkingHour() {
  let start = document.getElementById('wh-start').value;
  let end = document.getElementById('wh-end').value;
  
  if (start && start.length === 5) start += ':00';
  if (end && end.length === 5) end += ':00';

  const body = {
    dayOfWeek: document.getElementById('wh-day').value,
    startTime: start,
    endTime:   end
  };
  try {
    if (editingWhId) {
      await api(`/api/working-hours/${editingWhId}/update`, { method: 'PUT', body: JSON.stringify(body) });
    } else {
      await api('/api/working-hours/set', { method: 'POST', body: JSON.stringify(body) });
    }
    toast('Uğurlu!', 'İş saatı yadda saxlanıldı.');
    closeWhModal();
    loadWorkingHours();
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── PROFILE ──────────────────────────────────────────────
// UpdateUserRequest: { fullName: @NotBlank, phone: @NotBlank @Pattern(+994...), email: @NotBlank @Email }
async function updateProfile() {
  const fullName = document.getElementById('upd-name').value.trim();
  const phone    = document.getElementById('upd-phone').value.trim();
  const u        = getUser();
  const email    = u.email || '';
  if (!fullName || !phone) return toast('Xəta', 'Bütün xanaları doldurun.', 'error');
  if (!email)              return toast('Xəta', 'E-poçt tapılmadı. Yenidən daxil olun.', 'error');
  try {
    await api('/api/users/update', { method: 'PUT', body: JSON.stringify({ fullName, phone, email }) });
    u.fullName = fullName; u.phone = phone;
    localStorage.setItem('salon_user', JSON.stringify(u));
    document.getElementById('hdr-name').textContent    = fullName;
    document.getElementById('prof-name').textContent   = fullName;
    document.getElementById('hdr-avatar').textContent  = fullName[0].toUpperCase();
    document.getElementById('prof-avatar').textContent = fullName[0].toUpperCase();
    toast('Uğurlu!', 'Profil yeniləndi.');
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ChangePasswordRequest: { oldPassword @NotBlank @Size(min=6), newPassword @Pattern(^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$) }
async function changePassword() {
  const oldPassword = document.getElementById('old-pass').value;
  const newPassword = document.getElementById('new-pass').value;
  const c2          = document.getElementById('new-pass2').value;
  if (!oldPassword || !newPassword) return toast('Xəta', 'Şifrələri daxil edin.', 'error');
  if (newPassword !== c2)           return toast('Xəta', 'Yeni şifrələr uyğun deyil!', 'error');
  if (newPassword.length < 6)       return toast('Xəta', 'Ən az 6 simvol.', 'error');
  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/.test(newPassword))
    return toast('Xəta', 'Şifrə ən az bir hərf və bir rəqəm içərməlidir.', 'error');
  try {
    await api('/api/users/change-password', { method: 'PUT', body: JSON.stringify({ oldPassword, newPassword }) });
    toast('Uğurlu!', 'Şifrə dəyişdirildi.');
    ['old-pass', 'new-pass', 'new-pass2'].forEach(id => { document.getElementById(id).value = ''; });
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}
