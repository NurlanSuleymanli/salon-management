/**
 * customer.js — Customer Panel Logic
 * Salons, Booking Wizard, Reservation History, Profile
 *
 * Fixes applied vs backend DTOs:
 *  1. SalonResponse: uses contactPhone / contactEmail (not phone/city)
 *  2. ServiceResponseDto: has NO id field →
 *     use GET /api/services/salon/{salonId}/with-barbers (ServiceWithBarbersResponseDto)
 *     which DOES include id, filtered to services the chosen barber offers
 *  3. UpdateUserRequest: requires { fullName, phone, email } — all @NotBlank
 *  4. ReservationRequest: { barberId, serviceIds, date, startTime }
 *  5. ChangePasswordRequest: { oldPassword, newPassword }
 */
import {
  api, toast, doLogout, guardRoute, getUser, showSection,
  loadingHTML, emptyHTML, formatDate, saveTokens,
  STATUS_AZ, STATUS_CLS
} from './shared.js';

const NAV = ['salons', 'booking', 'history', 'profile'];

// ── BOOKING STATE ─────────────────────────────────────────
const bk = {
  salonId:    null,
  barberId:   null,
  barberName: '',
  // Each entry: { id: Long, name, durationMin, price }
  services:   [],
  date:       '',
  slot:       ''
};

// ── INIT ─────────────────────────────────────────────────
(function init() {
  if (!guardRoute()) return;

  const u    = getUser();
  const init = (u.fullName || 'U')[0].toUpperCase();

  document.getElementById('hdr-avatar').textContent  = init;
  document.getElementById('hdr-name').textContent    = u.fullName || u.email || '';
  document.getElementById('prof-avatar').textContent = init;
  document.getElementById('prof-name').textContent   = u.fullName || '';
  document.getElementById('prof-email').textContent  = u.email    || '';

  if (u.fullName) document.getElementById('upd-name').value  = u.fullName;
  if (u.phone)    document.getElementById('upd-phone').value = u.phone;
  if (u.email)    document.getElementById('upd-email').value = u.email;

  // Expose globals for HTML onclick handlers
  window.showSection       = (id) => _showSection(id);
  window.doLogout          = doLogout;
  window.searchSalons      = searchSalons;
  window.loadSalons        = loadSalons;
  window.openBooking       = openBooking;
  window.selectBarber      = selectBarber;
  window.toggleService     = toggleService;
  window.goStep            = goStep;
  window.loadSlots         = loadSlots;
  window.selectSlot        = selectSlot;
  window.createReservation = createReservation;
  window.cancelRes         = cancelRes;
  window.updateProfile     = updateProfile;
  window.changePassword    = changePassword;

  loadSalons();
})();

function _showSection(id) {
  showSection(id, NAV);
  if (id === 'history') loadHistory();
}

// ── SALONS ───────────────────────────────────────────────
async function loadSalons(query = '') {
  const grid = document.getElementById('salons-grid');
  grid.innerHTML = loadingHTML();
  try {
    // SalonResponse: { id, name, address, contactPhone, contactEmail, isActive }
    const url = query
      ? `/api/salons/search?name=${encodeURIComponent(query)}&page=0&size=20`
      : '/api/salons/list?page=0&size=20';
    const data   = await api(url, { auth: false });
    const salons = data.content || [];
    if (!salons.length) {
      grid.innerHTML = emptyHTML('🏪', 'Salon tapılmadı', 'Başqa axtarış sözcüyü cəhd edin');
      return;
    }
    const emojis = ['✂️', '💈', '🪒', '💅', '🧴', '🛁'];
    grid.innerHTML = salons.map(s => `
      <div class="salon-card" onclick="openBooking(${s.id}, '${(s.name || '').replace(/'/g, "\\'")}')">
        <div class="salon-card-badge ${s.isActive !== false ? 'badge-active' : 'badge-inactive'}">
          ${s.isActive !== false ? '✓ Aktiv' : 'Bağlı'}
        </div>
        <div class="salon-card-img">${emojis[s.id % emojis.length]}</div>
        <div class="salon-card-body">
          <h3>${s.name || 'Salon'}</h3>
          <p>${s.address || 'Ünvan məlumatı yoxdur'}</p>
          <div class="salon-meta">
            ${s.contactPhone ? `<span class="meta-chip">📞 ${s.contactPhone}</span>` : ''}
            ${s.contactEmail ? `<span class="meta-chip">✉️ ${s.contactEmail}</span>` : ''}
          </div>
          <button class="btn-view">Rezervasiya et →</button>
        </div>
      </div>`).join('');
  } catch(e) {
    grid.innerHTML = emptyHTML('⚠️', 'Xəta baş verdi', e.message);
  }
}

function searchSalons() {
  loadSalons(document.getElementById('salon-search').value.trim());
}

// ── BOOKING WIZARD ────────────────────────────────────────
async function openBooking(salonId, salonName) {
  bk.salonId    = salonId;
  bk.barberId   = null;
  bk.barberName = '';
  bk.services   = [];
  bk.date       = '';
  bk.slot       = '';
  document.getElementById('booking-salon-name').textContent = salonName;
  _showSection('booking');
  goStep(1);
  await loadBarbers(salonId);
}

function goStep(n) {
  for (let i = 1; i <= 4; i++) {
    const num  = document.getElementById('snum-' + i);
    const lbl  = document.getElementById('slbl-' + i);
    const step = document.getElementById('wstep-' + i);
    if (i < n)       { num.className = 'step-num done';   num.textContent = '✓'; lbl.classList.remove('active'); }
    else if (i === n){ num.className = 'step-num active'; num.textContent = i;   lbl.classList.add('active'); }
    else             { num.className = 'step-num idle';   num.textContent = i;   lbl.classList.remove('active'); }
    step.classList.toggle('active', i === n);
    const div = document.getElementById('sdiv-' + i);
    if (div) div.classList.toggle('done', i < n);
  }
  if (n === 2 && bk.barberId) loadBarberServices();
  if (n === 3) {
    const today = new Date().toISOString().split('T')[0];
    const dateEl = document.getElementById('res-date');
    dateEl.min = today; dateEl.value = '';
    document.getElementById('slots-container').innerHTML = '<p class="slot-loading">Tarix seçin...</p>';
    document.getElementById('btn-step3-next').disabled = true;
  }
  if (n === 4) renderConfirm();
}

// ── STEP 1: BARBERS ───────────────────────────────────────
async function loadBarbers(salonId) {
  const el = document.getElementById('barber-list');
  el.innerHTML = loadingHTML();
  try {
    // Returns List<BarberResponseDto>:
    // { id, userId, salonId, salonName, displayName, services[], isActive }
    const barbers = await api(`/api/barbers/salon/${salonId}`, { auth: false });
    const active  = barbers.filter(b => b.isActive !== false);
    if (!active.length) {
      el.innerHTML = emptyHTML('✂️', 'Bərbər yoxdur', 'Bu salonda aktiv bərbər tapılmadı');
      return;
    }
    el.innerHTML = active.map(b => `
      <div class="barber-card" id="bc-${b.id}"
           onclick="selectBarber(${b.id}, '${(b.displayName || '').replace(/'/g, "\\'")}')">
        <div class="barber-avatar">✂️</div>
        <div class="barber-name">${b.displayName || 'Bərbər'}</div>
        <div class="barber-spec">💈 ${b.services?.length || 0} xidmət</div>
      </div>`).join('');
  } catch(e) {
    el.innerHTML = emptyHTML('⚠️', 'Xəta', e.message);
  }
}

function selectBarber(id, name) {
  document.querySelectorAll('.barber-card').forEach(c => c.classList.remove('selected'));
  document.getElementById('bc-' + id)?.classList.add('selected');
  bk.barberId   = id;
  bk.barberName = name;
  bk.services   = []; // reset selected services when barber changes
  document.getElementById('btn-step1-next').disabled = false;
}

// ── STEP 2: SERVICES ──────────────────────────────────────
// Use GET /api/barbers/{id}/services which returns the barber's own assigned services.
// ServiceResponseDto: { id, name, durationMin, price, salonId, salonName }

async function loadBarberServices() {
  const el = document.getElementById('svc-list');
  el.innerHTML = loadingHTML();
  bk.services = [];
  updateSvcSummary();

  try {
    // Directly get this barber's services — works for both admin-assigned and self-created services
    const barberSvcs = await api(`/api/barbers/${bk.barberId}/services`, { auth: false });

    if (!barberSvcs.length) {
      el.innerHTML = emptyHTML('💈', 'Xidmət yoxdur', 'Bu bərbərin xidmət siyahısı boşdur');
      return;
    }

    el.innerHTML = barberSvcs.map(s => `
      <div class="svc-card" id="sc-${s.id}"
           onclick="toggleService(${s.id}, '${(s.name || '').replace(/'/g, "\\'")}', ${s.durationMin || 30}, ${Number(s.price) || 0})">
        <div class="svc-check">✓</div>
        <div class="svc-name">${s.name}</div>
        <div class="svc-dur">⏱ ${s.durationMin || 30} dəq</div>
        <div class="svc-price">${Number(s.price || 0).toFixed(2)} ₼</div>
      </div>`).join('');

  } catch(e) {
    el.innerHTML = emptyHTML('⚠️', 'Xidmətlər yüklənə bilmədi', e.message);
  }
}

// Toggle a service selection — id is the REAL DB id from ServiceWithBarbersResponseDto
function toggleService(id, name, durationMin, price) {
  const existing = bk.services.findIndex(s => s.id === id);
  if (existing === -1) {
    bk.services.push({ id, name, durationMin: parseInt(durationMin), price: parseFloat(price) });
  } else {
    bk.services.splice(existing, 1);
  }
  document.getElementById('sc-' + id)?.classList.toggle('selected', bk.services.some(s => s.id === id));
  updateSvcSummary();
  document.getElementById('btn-step2-next').disabled = bk.services.length === 0;
}

function updateSvcSummary() {
  const el    = document.getElementById('svc-summary');
  const items = document.getElementById('svc-summary-items');
  if (!bk.services.length) { el.classList.add('hidden'); return; }
  el.classList.remove('hidden');
  const totalDur   = bk.services.reduce((a, s) => a + s.durationMin, 0);
  const totalPrice = bk.services.reduce((a, s) => a + s.price, 0);
  items.innerHTML = bk.services
    .map(s => `<div class="sel-item"><span>${s.name}</span><span>${s.durationMin} dəq | ${s.price} ₼</span></div>`)
    .join('') +
    `<div class="sel-item"><span><strong>Cəmi</strong></span><span><strong>${totalDur} dəq | ${totalPrice.toFixed(2)} ₼</strong></span></div>`;
}

// ── STEP 3: DATE & SLOTS ─────────────────────────────────
async function loadSlots() {
  const date = document.getElementById('res-date').value;
  if (!date) return;
  bk.date = date; bk.slot = '';
  document.getElementById('btn-step3-next').disabled = true;
  const sc = document.getElementById('slots-container');
  sc.innerHTML = '<p class="slot-loading">⏳ Boş vaxtlar yüklənir...</p>';
  try {
    // Returns List<String> e.g. ["09:00", "09:30", ...]
    const totalDur = bk.services.reduce((a, s) => a + s.durationMin, 0) || 30;
    const slots = await api(`/api/barbers/${bk.barberId}/available-slots?date=${date}&duration=${totalDur}`, { auth: false });
    if (!slots.length) {
      sc.innerHTML = '<p class="slot-loading">Bu tarixdə boş vaxt yoxdur.</p>';
      return;
    }
    sc.innerHTML = '<div class="slots-grid">' +
      slots.map(sl => `<div class="slot" onclick="selectSlot('${sl}', this)">${sl}</div>`).join('') +
      '</div>';
  } catch(e) {
    sc.innerHTML = `<p class="slot-loading" style="color:var(--red)">Vaxtlar yüklənə bilmədi: ${e.message}</p>`;
  }
}

function selectSlot(s, el) {
  document.querySelectorAll('.slot').forEach(x => x.classList.remove('selected'));
  el.classList.add('selected');
  bk.slot = s;
  document.getElementById('btn-step3-next').disabled = false;
}

// ── STEP 4: CONFIRM & CREATE ──────────────────────────────
function renderConfirm() {
  const totalDur   = bk.services.reduce((a, s) => a + s.durationMin, 0);
  const totalPrice = bk.services.reduce((a, s) => a + s.price, 0);
  document.getElementById('confirm-details').innerHTML = `
    <div class="confirm-row"><span class="confirm-label">Bərbər</span><span>${bk.barberName}</span></div>
    <div class="confirm-row"><span class="confirm-label">Xidmətlər</span><span>${bk.services.map(s => s.name).join(', ')}</span></div>
    <div class="confirm-row"><span class="confirm-label">Tarix</span><span>${bk.date}</span></div>
    <div class="confirm-row"><span class="confirm-label">Başlanğıc vaxtı</span><span>${bk.slot}</span></div>
    <div class="confirm-row"><span class="confirm-label">Müddət</span><span>${totalDur} dəqiqə</span></div>
    <div class="confirm-row"><span class="confirm-label">Ümumi məbləğ</span><span>${totalPrice.toFixed(2)} ₼</span></div>`;
}

async function createReservation() {
  const btn = document.getElementById('btn-confirm');
  btn.disabled = true; btn.textContent = '⏳ Göndərilir...';
  try {
    if (!bk.barberId)          throw new Error('Bərbər seçilməyib!');
    if (!bk.services.length)   throw new Error('Xidmət seçilməyib!');
    if (!bk.date)              throw new Error('Tarix seçilməyib!');
    if (!bk.slot)              throw new Error('Vaxt seçilməyib!');

    // ReservationRequest: { barberId: Long, serviceIds: List<Long>, date: LocalDate, startTime: LocalTime }
    // startTime must include seconds for LocalTime deserialization → "HH:mm:00"
    const startTime = bk.slot.includes(':') && bk.slot.split(':').length === 2
      ? bk.slot + ':00'
      : bk.slot;

    await api('/api/reservations/create', {
      method: 'POST',
      body: JSON.stringify({
        barberId:   bk.barberId,
        serviceIds: bk.services.map(s => s.id),
        date:       bk.date,
        startTime:  startTime
      })
    });

    toast('✅ Uğurlu!', 'Rezervasiyanız yaradıldı. Bərbər tərəfindən təsdiq gözləyir.');
    bk.services = []; // reset
    _showSection('history');
  } catch(e) {
    toast('Xəta', e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '✅ Rezervasiyanı Yarat';
  }
}

// ── RESERVATION HISTORY ───────────────────────────────────
async function loadHistory() {
  const el = document.getElementById('history-body');
  el.innerHTML = loadingHTML();
  try {
    // Returns List<ReservationResponseDto>:
    // { id, customerId, customerName, barberId, barberDisplayName, salonId,
    //   salonName, serviceIds[], serviceNames[], totalDurationMin,
    //   totalPrice, startAt(Instant), endAt, status, cancelledAt, cancelReason }
    const list = await api('/api/reservations/my-history');
    if (!list.length) {
      el.innerHTML = emptyHTML('📋', 'Rezervasiya yoxdur', 'Hələ heç bir rezervasiya etməmisiniz.');
      return;
    }
    el.innerHTML = `
      <table class="res-table">
        <thead>
          <tr>
            <th>Bərbər / Salon</th>
            <th>Xidmətlər</th>
            <th>Tarix & Vaxt</th>
            <th>Məbləğ</th>
            <th>Status</th>
            <th>Əməliyyat</th>
          </tr>
        </thead>
        <tbody>
          ${list.map(r => {
            const st = r.status || 'PENDING';
            const canCancel = (st === 'PENDING' || st === 'CONFIRMED');
            return `<tr>
              <td>
                <strong>${r.barberDisplayName || '—'}</strong><br>
                <small style="color:var(--gray-400)">${r.salonName || '—'}</small>
              </td>
              <td>${(r.serviceNames || []).join('<br>') || '—'}</td>
              <td style="white-space:nowrap">${formatDate(r.startAt)}</td>
              <td><strong>${r.totalPrice || 0} ₼</strong></td>
              <td><span class="status-badge ${STATUS_CLS[st] || 'badge-pending'}">${STATUS_AZ[st] || st}</span></td>
              <td>${canCancel
                ? `<button class="btn-cancel-res" onclick="cancelRes(${r.id})">Ləğv et</button>`
                : '<span style="color:var(--gray-400);font-size:.8rem">—</span>'
              }</td>
            </tr>`;
          }).join('')}
        </tbody>
      </table>`;
  } catch(e) {
    el.innerHTML = emptyHTML('⚠️', 'Xəta', e.message);
  }
}

async function cancelRes(id) {
  if (!confirm('Bu rezervasiyanı ləğv etmək istəyirsiniz?\n(Ən az 2 saat əvvəl ləğv edilməlidir)')) return;
  try {
    // PUT /api/reservations/{id}/cancel
    await api(`/api/reservations/${id}/cancel`, { method: 'PUT' });
    toast('Ləğv edildi', 'Rezervasiyanız uğurla ləğv edildi.');
    loadHistory();
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ── PROFILE ──────────────────────────────────────────────
// UpdateUserRequest: { fullName: @NotBlank, phone: @NotBlank @Pattern(+994...), email: @NotBlank @Email }
async function updateProfile() {
  const fullName = document.getElementById('upd-name').value.trim();
  const phone    = document.getElementById('upd-phone').value.trim();
  const emailEl  = document.getElementById('upd-email');
  const u        = getUser();
  const email    = (emailEl ? emailEl.value.trim() : '') || u.email || '';

  if (!fullName)  return toast('Xəta', 'Ad Soyad daxil edin.', 'error');
  if (!phone)     return toast('Xəta', 'Telefon daxil edin.', 'error');
  if (!email)     return toast('Xəta', 'E-poçt tapılmadı. Yenidən daxil olun.', 'error');

  try {
    // PUT /api/users/update
    await api('/api/users/update', {
      method: 'PUT',
      body:   JSON.stringify({ fullName, phone, email })
    });
    u.fullName = fullName; u.phone = phone; u.email = email;
    localStorage.setItem('salon_user', JSON.stringify(u));
    document.getElementById('hdr-name').textContent    = fullName;
    document.getElementById('prof-name').textContent   = fullName;
    document.getElementById('hdr-avatar').textContent  = fullName[0].toUpperCase();
    document.getElementById('prof-avatar').textContent = fullName[0].toUpperCase();
    toast('Uğurlu!', 'Profiliniz yeniləndi.');
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}

// ChangePasswordRequest: { oldPassword: @NotBlank @Size(min=6), newPassword: @NotBlank @Pattern(^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$) }
async function changePassword() {
  const oldPassword = document.getElementById('old-pass').value;
  const newPassword = document.getElementById('new-pass').value;
  const confirm2    = document.getElementById('new-pass2').value;

  if (!oldPassword || !newPassword) return toast('Xəta', 'Şifrələri daxil edin.', 'error');
  if (newPassword !== confirm2)     return toast('Xəta', 'Yeni şifrələr uyğun gəlmir!', 'error');
  if (newPassword.length < 6)       return toast('Xəta', 'Ən az 6 simvol.', 'error');
  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,}$/.test(newPassword))
    return toast('Xəta', 'Şifrə ən az bir hərf və bir rəqəm içərməlidir.', 'error');

  try {
    await api('/api/users/change-password', {
      method: 'PUT',
      body:   JSON.stringify({ oldPassword, newPassword })
    });
    toast('Uğurlu!', 'Şifrəniz dəyişdirildi.');
    ['old-pass', 'new-pass', 'new-pass2'].forEach(id => { document.getElementById(id).value = ''; });
  } catch(e) {
    toast('Xəta', e.message, 'error');
  }
}
