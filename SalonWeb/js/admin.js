/**
 * admin.js — Admin Panel Logic
 * Dashboard, Salons, Barbers, Services, Reservations, Users
 */
import {
  api, toast, doLogout, guardRoute, getUser,
  loadingHTML, emptyHTML, buildPagination, formatDate,
  STATUS_AZ, STATUS_CLS, saveTokens
} from './shared.js';

const PAGE_SIZE = 10;
const pages     = { salons: 0, barbers: 0, services: 0, reservations: 0, users: 0 };
const editId    = { salon: null, barber: null, service: null };
let salonsList   = [];
let resStatusId  = null;
let resStatusVal = null;

const SECTION_TITLES = {
  dashboard:    'İdarə Paneli',
  salons:       'Salonlar',
  barbers:      'Bərbərlər',
  services:     'Xidmətlər',
  reservations: 'Rezervasiyalar',
  users:        'İstifadəçilər'
};

// ── INIT ─────────────────────────────────────────────────
(function init() {
  if (!guardRoute()) return;
  const u    = getUser();
  const init = (u.fullName || 'A')[0].toUpperCase();
  document.getElementById('sb-avatar').textContent = init;
  document.getElementById('sb-name').textContent   = u.fullName || u.email || 'Admin';

  // Expose globals
  window.doLogout              = doLogout;
  window.showSection           = showSection;
  window.closeModal            = closeModal;
  window.loadSalons            = () => loadSalons(pages.salons);
  window.openSalonModal        = openSalonModal;
  window.saveSalon             = saveSalon;
  window.deleteSalon           = deleteSalon;
  window.openBarberModal       = openBarberModal;
  window.saveBarber            = saveBarber;
  window.toggleBarberStatus    = toggleBarberStatus;
  window.deleteBarber          = deleteBarber;
  window.openServiceModal      = openServiceModal;
  window.saveService           = saveService;
  window.deleteService         = deleteService;
  window.onSvcSalonChange      = onSvcSalonChange;
  window.openResStatusModal    = openResStatusModal;
  window.saveResStatus         = saveResStatus;
  window.toggleUserStatus      = toggleUserStatus;
  window.makeAdmin             = makeAdmin;

  loadDashboard();
  loadSalonsList();
})();

// ── SECTION NAVIGATION ────────────────────────────────────
function showSection(id) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById('sec-' + id)?.classList.add('active');
  // Match clicked nav-item by text
  document.querySelectorAll('.nav-item').forEach(n => {
    if (n.textContent.toLowerCase().includes(SECTION_TITLES[id]?.slice(0, 5).toLowerCase()))
      n.classList.add('active');
  });
  document.getElementById('topbar-title').textContent = SECTION_TITLES[id] || id;

  const loaders = {
    salons:       () => loadSalons(0),
    barbers:      () => loadBarbers(0),
    services:     () => loadServices(0),
    reservations: () => loadReservations(0),
    users:        () => loadUsers(0)
  };
  loaders[id]?.();
}

function closeModal(id) {
  document.getElementById(id)?.classList.add('hidden');
}

// ── DASHBOARD ─────────────────────────────────────────────
async function loadDashboard() {
  const statsEl = document.getElementById('dash-stats');
  try {
    const [sData, bData, rData, uData] = await Promise.all([
      api('/api/salons/list?page=0&size=1',               { auth: false }),
      api('/api/barbers/list?page=0&size=1',              { auth: false }),
      api('/api/reservations/all?page=0&size=6'),
      api('/api/users/all?page=0&size=1')
    ]);
    const pg = d => d?.page || d; // Spring Boot 3: { page: { totalElements, totalPages } } or old flat
    statsEl.innerHTML = `
      <div class="stat-card"><div class="stat-icon si-blue">🏪</div><div><div class="stat-val">${pg(sData).totalElements || 0}</div><div class="stat-label">Cəmi Salon</div></div></div>
      <div class="stat-card"><div class="stat-icon si-violet">✂️</div><div><div class="stat-val">${pg(bData).totalElements || 0}</div><div class="stat-label">Cəmi Bərbər</div></div></div>
      <div class="stat-card"><div class="stat-icon si-amber">📋</div><div><div class="stat-val">${pg(rData).totalElements || 0}</div><div class="stat-label">Cəmi Rezervasiya</div></div></div>
      <div class="stat-card"><div class="stat-icon si-green">👥</div><div><div class="stat-val">${pg(uData).totalElements || 0}</div><div class="stat-label">Cəmi İstifadəçi</div></div></div>`;
    renderResTable(rData.content || [], document.getElementById('dash-recent-body'), true);
  } catch(e) {
    statsEl.innerHTML = `<div style="color:var(--red);padding:1rem">Xəta: ${e.message}</div>`;
  }
}

// ── SALONS ────────────────────────────────────────────────
async function loadSalons(p = 0) {
  pages.salons = p;
  const tbl = document.getElementById('salon-table');
  tbl.innerHTML = loadingHTML();
  const q = document.getElementById('salon-q')?.value?.trim() || '';
  try {
    const url = q
      ? `/api/salons/search?name=${encodeURIComponent(q)}&page=${p}&size=${PAGE_SIZE}`
      : `/api/salons/list?page=${p}&size=${PAGE_SIZE}`;
    const data = await api(url, { auth: false });
    const salons = data.content || [];
    tbl.outerHTML = buildSalonTable(salons);
    buildPagination('salon-pg', data.page?.totalPages ?? data.totalPages ?? 1, p, pg => loadSalons(pg));
  } catch(e) {
    document.getElementById('salon-table').innerHTML = `<tr><td style="color:var(--red);padding:1rem">${e.message}</td></tr>`;
  }
}

function buildSalonTable(salons) {
  if (!salons.length) return `<table id="salon-table" class="data-table"><tbody><tr><td>${emptyHTML('🏪', 'Salon tapılmadı')}</td></tr></tbody></table>`;
  return `<table id="salon-table" class="data-table">
    <thead><tr><th>#</th><th>Ad</th><th>Ünvan</th><th>Əlaqə Telefonu</th><th>E-poçt</th><th>Status</th><th>Əməliyyat</th></tr></thead>
    <tbody>
      ${salons.map(s => `<tr>
        <td>${s.id}</td>
        <td><strong>${s.name || '—'}</strong></td>
        <td>${s.address || '—'}</td>
        <td>${s.contactPhone || '—'}</td>
        <td>${s.contactEmail || '—'}</td>
        <td><span class="status-badge ${s.isActive !== false ? 'badge-active' : 'badge-inactive'}">${s.isActive !== false ? 'Aktiv' : 'Deaktiv'}</span></td>
        <td><div class="action-btns">
          <button class="btn-tbl btn-edit" onclick='openSalonModal(${JSON.stringify(s)})'>Düzəlt</button>
          <button class="btn-tbl btn-del"  onclick="deleteSalon(${s.id})">Sil</button>
        </div></td>
      </tr>`).join('')}
    </tbody></table>`;
}

function openSalonModal(s) {
  editId.salon = s ? s.id : null;
  document.getElementById('sm-title').textContent        = s ? 'Salon Düzəlt' : 'Salon Əlavə Et';
  document.getElementById('sm-name').value               = s?.name         || '';
  document.getElementById('sm-address').value            = s?.address       || '';
  document.getElementById('sm-contact-phone').value      = s?.contactPhone  || '';
  document.getElementById('sm-contact-email').value      = s?.contactEmail  || '';
  document.getElementById('salon-modal').classList.remove('hidden');
}

async function saveSalon() {
  const body = {
    name:         document.getElementById('sm-name').value.trim(),
    address:      document.getElementById('sm-address').value.trim(),
    contactPhone: document.getElementById('sm-contact-phone').value.trim(),
    contactEmail: document.getElementById('sm-contact-email').value.trim()
  };
  if (!body.name)         return toast('Xəta', 'Salon adı daxil edin.', 'error');
  if (!body.address)      return toast('Xəta', 'Ünvan daxil edin.', 'error');
  if (!body.contactPhone) return toast('Xəta', 'Telefon daxil edin (+994501234567).', 'error');
  if (!body.contactEmail) return toast('Xəta', 'E-poçt daxil edin.', 'error');
  try {
    if (editId.salon) await api(`/api/salons/${editId.salon}/update`, { method: 'PUT',  body: JSON.stringify(body) });
    else              await api('/api/salons/create',                  { method: 'POST', body: JSON.stringify(body) });
    toast('Uğurlu!', editId.salon ? 'Salon yeniləndi.' : 'Salon yaradıldı.');
    closeModal('salon-modal');
    loadSalons(0);
    loadSalonsList();
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function deleteSalon(id) {
  if (!confirm('Bu salonu silmək istədiyinizdən əminsiniz?')) return;
  try {
    await api(`/api/salons/${id}/delete`, { method: 'DELETE' });
    toast('Silindi', 'Salon silindi.');
    loadSalons(0);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function loadSalonsList() {
  try {
    const data = await api('/api/salons/list?page=0&size=100', { auth: false });
    salonsList = data.content || [];
    ['bm-salon', 'svc-salon'].forEach(id => {
      const el = document.getElementById(id);
      if (!el) return;
      el.innerHTML = '<option value="">Salon seçin...</option>' +
        salonsList.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
    });
  } catch(e) {}
}

// ── BARBERS ───────────────────────────────────────────────
async function loadBarbers(p = 0) {
  pages.barbers = p;
  const tbl = document.getElementById('barber-table');
  tbl.innerHTML = loadingHTML();
  try {
    const data    = await api(`/api/barbers/list?page=${p}&size=${PAGE_SIZE}`, { auth: false });
    const barbers = data.content || [];
    tbl.outerHTML = buildBarberTable(barbers);
    buildPagination('barber-pg', data.page?.totalPages ?? data.totalPages ?? 1, p, pg => loadBarbers(pg));
  } catch(e) {
    document.getElementById('barber-table').innerHTML = `<tr><td style="color:var(--red)">${e.message}</td></tr>`;
  }
}

function buildBarberTable(barbers) {
  if (!barbers.length) return `<table id="barber-table" class="data-table"><tbody><tr><td>${emptyHTML('✂️', 'Bərbər yoxdur')}</td></tr></tbody></table>`;
  return `<table id="barber-table" class="data-table">
    <thead><tr><th>#</th><th>Bərbər</th><th>Salon</th><th>İxtisas</th><th>Status</th><th>Əməliyyat</th></tr></thead>
    <tbody>
      ${barbers.map(b => `<tr>
        <td>${b.id}</td>
        <td><strong>${b.displayName || b.fullName || '—'}</strong></td>
        <td>${b.salonName || '—'}</td>
        <td>${b.specialization || '—'}</td>
        <td><span class="status-badge ${b.isActive !== false ? 'badge-active' : 'badge-inactive'}">${b.isActive !== false ? 'Aktiv' : 'Deaktiv'}</span></td>
        <td><div class="action-btns">
          <button class="btn-tbl btn-edit"   onclick='openBarberModal(${JSON.stringify(b)})'>Düzəlt</button>
          <button class="btn-tbl btn-toggle" onclick="toggleBarberStatus(${b.id})">${b.isActive !== false ? 'Deaktiv' : 'Aktiv'}</button>
          <button class="btn-tbl btn-del"    onclick="deleteBarber(${b.id})">Sil</button>
        </div></td>
      </tr>`).join('')}
    </tbody></table>`;
}

function openBarberModal(b) {
  editId.barber = b ? b.id : null;
  document.getElementById('bm-title').textContent   = b ? 'Bərbər Düzəlt' : 'Bərbər Əlavə Et';
  document.getElementById('bm-display').value       = b?.displayName || '';
  document.getElementById('bm-salon').value         = b?.salonId     || '';
  document.getElementById('bm-user').value          = b?.userId      || '';
  // Hide User ID input for existing barbers
  const userGroup = document.getElementById('bm-user').closest('.form-group');
  if (b) {
      userGroup.style.display = 'none';
  } else {
      userGroup.style.display = 'block';
  }

  document.getElementById('barber-modal').classList.remove('hidden');
}

async function saveBarber() {
  // If editing, we don't pass userId (or we read the existing hidden one, but backend ignores it on update)
  const userId = editId.barber ? null : parseInt(document.getElementById('bm-user').value);
  const body = {
    displayName: document.getElementById('bm-display').value.trim(),
    salonId:     parseInt(document.getElementById('bm-salon').value) || null,
    userId:      userId || null,
    serviceIds:  []
  };
  if (!body.displayName) return toast('Xəta', 'Ekran adı daxil edin.', 'error');
  if (!body.salonId)     return toast('Xəta', 'Salon seçin.', 'error');
  if (!editId.barber && !body.userId) return toast('Xəta', 'İstifadəçi ID daxil edin (Bərbər hesabının ID-si).', 'error');
  try {
    if (editId.barber) await api(`/api/barbers/${editId.barber}/update`, { method: 'PUT',  body: JSON.stringify(body) });
    else               await api('/api/barbers/add',                      { method: 'POST', body: JSON.stringify(body) });
    toast('Uğurlu!', editId.barber ? 'Bərbər yeniləndi.' : 'Bərbər əlavə edildi.');
    closeModal('barber-modal');
    loadBarbers(0);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function toggleBarberStatus(id) {
  try {
    await api(`/api/barbers/${id}/status`, { method: 'PUT' });
    toast('Uğurlu!', 'Bərbər statusu dəyişdirildi.');
    loadBarbers(pages.barbers);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function deleteBarber(id) {
  if (!confirm('Bərbəri silmək istədiyinizə əminsiniz?')) return;
  try {
    await api(`/api/barbers/${id}/delete`, { method: 'DELETE' });
    toast('Silindi', 'Bərbər silindi.');
    loadBarbers(0);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── SERVICES ──────────────────────────────────────────────
async function loadServices(p = 0) {
  pages.services = p;
  const tbl = document.getElementById('service-table');
  tbl.innerHTML = loadingHTML();
  try {
    const data = await api(`/api/services/list?page=${p}&size=${PAGE_SIZE}`, { auth: false });
    const svcs = data.content || [];
    tbl.outerHTML = buildServiceTable(svcs);
    buildPagination('service-pg', data.page?.totalPages ?? data.totalPages ?? 1, p, pg => loadServices(pg));
  } catch(e) {
    document.getElementById('service-table').innerHTML = `<tr><td style="color:var(--red)">${e.message}</td></tr>`;
  }
}

function buildServiceTable(svcs) {
  if (!svcs.length) return `<table id="service-table" class="data-table"><tbody><tr><td>${emptyHTML('💈', 'Xidmət yoxdur')}</td></tr></tbody></table>`;
  return `<table id="service-table" class="data-table">
    <thead><tr><th>#</th><th>Xidmət</th><th>Salon</th><th>Müddət</th><th>Qiymət</th><th>Əməliyyat</th></tr></thead>
    <tbody>
      ${svcs.map(s => `<tr>
        <td>${s.id}</td>
        <td><strong>${s.name || '—'}</strong></td>
        <td>${s.salonName || '—'}</td>
        <td>⏱ ${s.durationMin || 0} dəq</td>
        <td><strong>${s.price || 0} ₼</strong></td>
        <td><div class="action-btns">
          <button class="btn-tbl btn-edit" onclick='openServiceModal(${JSON.stringify(s)})'>Düzəlt</button>
          <button class="btn-tbl btn-del"  onclick="deleteService(${s.id})">Sil</button>
        </div></td>
      </tr>`).join('')}
    </tbody></table>`;
}

function openServiceModal(s) {
  editId.service = s ? s.id : null;
  document.getElementById('svc-title').textContent = s ? 'Xidməti Düzəlt' : 'Xidmət Əlavə Et';
  document.getElementById('svc-name').value        = s?.name        || '';
  document.getElementById('svc-dur').value         = s?.durationMin || 30;
  document.getElementById('svc-price').value       = s?.price       || 0;
  document.getElementById('svc-salon').value       = s?.salonId     || '';
  
  // Store existing barber selections globally so onSvcSalonChange can check them
  window._svcExistingBarbers = s?.barberIds || [];
  
  if (s?.salonId) {
      onSvcSalonChange();
  } else {
      document.getElementById('svc-barbers-group').style.display = 'none';
      document.getElementById('svc-barbers-list').innerHTML = '';
  }

  document.getElementById('service-modal').classList.remove('hidden');
}

async function onSvcSalonChange() {
  const salonId = document.getElementById('svc-salon').value;
  const listEl = document.getElementById('svc-barbers-list');
  const groupEl = document.getElementById('svc-barbers-group');
  
  if (!salonId) {
      groupEl.style.display = 'none';
      listEl.innerHTML = '';
      return;
  }
  
  groupEl.style.display = 'block';
  listEl.innerHTML = '<span style="color:var(--gray-400);font-size:0.9rem;">⏳ Yüklənir...</span>';
  
  try {
      const data = await api(`/api/barbers/salon/${salonId}`, { auth: false });
      if (!data || !data.length) {
          listEl.innerHTML = '<span style="color:var(--gray-400);font-size:0.9rem;">Bu salonda bərbər yoxdur.</span>';
          return;
      }
      
      const existing = window._svcExistingBarbers || [];
      listEl.innerHTML = data.map(b => `
          <label style="display:flex; align-items:center; gap:.5rem; cursor:pointer;">
             <input type="checkbox" class="svc-barber-cb" value="${b.id}" ${existing.includes(b.id) ? 'checked' : ''} />
             ${b.displayName || b.fullName}
          </label>
      `).join('');
  } catch(e) {
      listEl.innerHTML = `<span style="color:var(--red);font-size:0.9rem;">Xəta: ${e.message}</span>`;
  }
}

async function saveService() {
  const barberIds = Array.from(document.querySelectorAll('.svc-barber-cb:checked')).map(cb => parseInt(cb.value));
  const body = {
    name:        document.getElementById('svc-name').value.trim(),
    durationMin: parseInt(document.getElementById('svc-dur').value)     || 30,
    price:       parseFloat(document.getElementById('svc-price').value) || 0,
    salonId:     parseInt(document.getElementById('svc-salon').value)   || null,
    barberIds:   barberIds
  };
  if (!body.name || !body.salonId) return toast('Xəta', 'Ad və salon seçin.', 'error');
  try {
    if (editId.service) await api(`/api/services/${editId.service}/update`, { method: 'PUT',  body: JSON.stringify(body) });
    else                await api('/api/services/add',                        { method: 'POST', body: JSON.stringify(body) });
    toast('Uğurlu!', editId.service ? 'Xidmət yeniləndi.' : 'Xidmət əlavə edildi.');
    closeModal('service-modal');
    loadServices(0);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function deleteService(id) {
  if (!confirm('Xidməti silmək istəyirsiniz?')) return;
  try {
    await api(`/api/services/${id}/delete`, { method: 'DELETE' });
    toast('Silindi', 'Xidmət silindi.');
    loadServices(0);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── RESERVATIONS ──────────────────────────────────────────
async function loadReservations(p = 0) {
  pages.reservations = p;
  const tbl = document.getElementById('res-table');
  tbl.innerHTML = loadingHTML();
  try {
    const data = await api(`/api/reservations/all?page=${p}&size=${PAGE_SIZE}`);
    renderResTable(data.content || [], tbl, false);
    buildPagination('res-pg', data.page?.totalPages ?? data.totalPages ?? 1, p, pg => loadReservations(pg));
  } catch(e) {
    tbl.innerHTML = `<tr><td style="color:var(--red)">${e.message}</td></tr>`;
  }
}

function renderResTable(list, el, mini = false) {
  if (!list.length) { el.innerHTML = emptyHTML('📋', 'Rezervasiya yoxdur'); return; }
  const head = mini
    ? '<th>Müştəri</th><th>Xidmət</th><th>Vaxt</th><th>Status</th><th></th>'
    : '<th>#</th><th>Müştəri</th><th>Bərbər / Salon</th><th>Xidmətlər</th><th>Vaxt</th><th>Məbləğ</th><th>Status</th><th>Əməliyyat</th>';
  el.innerHTML = `
    <table class="data-table">
      <thead><tr>${head}</tr></thead>
      <tbody>
        ${list.map(r => {
          const st = r.status || 'PENDING';
          const dt = formatDate(r.startAt);
          const canChange = st !== 'CANCELLED' && st !== 'COMPLETED';
          if (mini) return `<tr>
            <td><strong>${r.customerName || '—'}</strong></td>
            <td>${(r.serviceNames || []).join(', ') || '—'}</td>
            <td>${dt}</td>
            <td><span class="status-badge ${STATUS_CLS[st]}">${STATUS_AZ[st] || st}</span></td>
            <td>${canChange ? `<button class="btn-tbl btn-status-res" onclick="openResStatusModal(${r.id},'${st}')">Status</button>` : '<span style="color:var(--gray-400);font-size:.8rem">—</span>'}</td>
          </tr>`;
          return `<tr>
            <td>${r.id}</td>
            <td><strong>${r.customerName || '—'}</strong></td>
            <td>${r.barberDisplayName || '—'}<br><small style="color:var(--gray-400)">${r.salonName || '—'}</small></td>
            <td>${(r.serviceNames || []).join(', ') || '—'}</td>
            <td>${dt}</td>
            <td><strong>${r.totalPrice || 0} ₼</strong></td>
            <td><span class="status-badge ${STATUS_CLS[st]}">${STATUS_AZ[st] || st}</span></td>
            <td>${canChange ? `<button class="btn-tbl btn-status-res" onclick="openResStatusModal(${r.id},'${st}')" title="Statusu dəyişdir">Dəyişdir</button>` : '<span style="color:var(--gray-400);font-size:.8rem">Son status</span>'}</td>
          </tr>`;
        }).join('')}
      </tbody>
    </table>`;
}

function openResStatusModal(id, current) {
  resStatusId  = id;
  resStatusVal = null;  // ← null saxla: istifadəçi mütləq yeni status seçməlidir
  // We don't rely only on inline onchange anymore.
  // Instead, on save, we find the checked radio.
  const selectableStatuses = [
    { val: 'CONFIRMED', disabled: current === 'CONFIRMED' },
    { val: 'COMPLETED', disabled: false },
    { val: 'CANCELLED', disabled: false }
  ];
  document.getElementById('status-options').innerHTML = selectableStatuses.map(({ val, disabled }) => `
    <label class="status-option ${disabled ? 'status-option-disabled' : ''}" ${disabled ? 'title="Artıq bu statusdadır"' : ''} onclick="if(!${disabled}){document.querySelectorAll('.status-option').forEach(o=>o.classList.remove('selected'));this.classList.add('selected')}">
      <input type="radio" name="res-status" value="${val}" ${disabled ? 'disabled' : ''} />
      <span class="status-badge ${STATUS_CLS[val]}">${STATUS_AZ[val] || val}</span>
      ${val === current ? '<small style="color:var(--gray-400)"> (cari)</small>' : ''}
    </label>`).join('');
  document.getElementById('res-status-modal').classList.remove('hidden');
}

async function saveResStatus() {
  if (!resStatusId) return;

  const checkedRadio = document.querySelector('input[name="res-status"]:checked');
  resStatusVal = checkedRadio ? checkedRadio.value : null;

  if (!resStatusVal) {
    toast('Xəbərdarlıq', 'Zəhmət olmasa yeni bir status seçin.', 'info');
    return;
  }
  try {
    await api(`/api/reservations/${resStatusId}/status`, {
      method: 'PUT',
      body:   JSON.stringify({ status: resStatusVal })
    });
    toast('Uğurlu!', `Rezervasiya statusu "${STATUS_AZ[resStatusVal] || resStatusVal}" olaraq yeniləndi.`);
    closeModal('res-status-modal');
    resStatusId  = null;
    resStatusVal = null;
    loadReservations(pages.reservations);
    // Dashboard-da da yeniləmək lazımdır
    loadDashboard();
  } catch(e) {
    toast('Xəta', e.message || 'Status dəyişdirilə bilmədi.', 'error');
  }
}

// ── USERS ─────────────────────────────────────────────────
async function loadUsers(p = 0) {
  pages.users = p;
  const tbl = document.getElementById('user-table');
  tbl.innerHTML = loadingHTML();
  const ROLE_CLS = { ADMIN: 'badge-admin', BARBER: 'badge-barber', CUSTOMER: 'badge-customer' };
  try {
    const data  = await api(`/api/users/all?page=${p}&size=${PAGE_SIZE}`);
    const users = data.content || [];
    tbl.outerHTML = !users.length
      ? `<table id="user-table" class="data-table"><tbody><tr><td>${emptyHTML('👥', 'İstifadəçi yoxdur')}</td></tr></tbody></table>`
      : `<table id="user-table" class="data-table">
          <thead><tr><th>#</th><th>Ad Soyad</th><th>E-poçt</th><th>Telefon</th><th>Rol</th><th>Status</th><th>Əməliyyat</th></tr></thead>
          <tbody>
            ${users.map(u => `<tr>
              <td>${u.id}</td>
              <td><strong>${u.fullName || '—'}</strong></td>
              <td>${u.email || '—'}</td>
              <td>${u.phone || '—'}</td>
              <td><span class="status-badge ${ROLE_CLS[u.role] || 'badge-customer'}">${u.role || 'CUSTOMER'}</span></td>
              <td><span class="status-badge ${u.isActive !== false ? 'badge-active' : 'badge-inactive'}">${u.isActive !== false ? 'Aktiv' : 'Deaktiv'}</span></td>
              <td><div class="action-btns">
                <button class="btn-tbl btn-toggle" onclick="toggleUserStatus(${u.id})">${u.isActive !== false ? 'Deaktiv' : 'Aktiv'}</button>
                ${u.role !== 'ADMIN' ? `<button class="btn-tbl btn-admin" onclick="makeAdmin(${u.id})">Admin et</button>` : ''}
              </div></td>
            </tr>`).join('')}
          </tbody>
        </table>`;
    buildPagination('user-pg', data.page?.totalPages ?? data.totalPages ?? 1, p, pg => loadUsers(pg));
  } catch(e) {
    document.getElementById('user-table').innerHTML = `<tr><td style="color:var(--red)">${e.message}</td></tr>`;
  }
}

async function toggleUserStatus(id) {
  try {
    await api(`/api/users/${id}/status`, { method: 'PUT' });
    toast('Uğurlu!', 'İstifadəçi statusu dəyişdirildi.');
    loadUsers(pages.users);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

async function makeAdmin(id) {
  if (!confirm('Bu istifadəçini admin etmək istəyirsiniz?')) return;
  try {
    await api(`/api/users/${id}/make-admin`, { method: 'PUT' });
    toast('Uğurlu!', 'İstifadəçi admin edildi.');
    loadUsers(pages.users);
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}
