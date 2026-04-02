// Global App State & UI Logic
let currentUser = null;
let selectedServiceIds = [];
let selectedBarberId = null;
let selectedBarberName = "";

// =======================
// UI UTILS
// =======================
function showLoader() { document.getElementById('global-loader').style.display = 'flex'; }
function hideLoader() { document.getElementById('global-loader').style.display = 'none'; }

function showToast(title, message, isError = false) {
    const toast = document.getElementById('toast');
    document.getElementById('toast-title').textContent = title;
    document.getElementById('toast-message').textContent = message;
    
    const iconLayout = document.getElementById('toast-icon');
    if(isError) {
        toast.className = "fixed top-5 right-5 z-[100] transform transition-all duration-300 max-w-sm w-full bg-white shadow-2xl rounded-2xl p-4 border-l-4 border-red-500 flex items-start gap-4";
        iconLayout.innerHTML = '<i class="fa-solid fa-circle-exclamation text-red-500"></i>';
    } else {
        toast.className = "fixed top-5 right-5 z-[100] transform transition-all duration-300 max-w-sm w-full bg-white shadow-2xl rounded-2xl p-4 border-l-4 border-brand-500 flex items-start gap-4";
        iconLayout.innerHTML = '<i class="fa-solid fa-bell text-brand-500"></i>';
    }
    
    toast.classList.remove('translate-x-full', 'opacity-0');
    setTimeout(() => {
        toast.classList.add('translate-x-full', 'opacity-0');
    }, 4000);
}

// =======================
// AUTH UI HANDLING
// =======================
function toggleAuthMode(mode) {
    const bg = document.getElementById('auth-switcher-bg');
    if (mode === 'login') {
        bg.style.transform = 'translateX(0)';
        document.getElementById('btn-mode-login').classList.replace('text-slate-500', 'text-brand-600');
        document.getElementById('btn-mode-register').classList.replace('text-brand-600', 'text-slate-500');
        document.getElementById('form-login').classList.remove('hidden');
        document.getElementById('form-register').classList.add('hidden');
    } else {
        bg.style.transform = 'translateX(100%)';
        document.getElementById('btn-mode-register').classList.replace('text-slate-500', 'text-brand-600');
        document.getElementById('btn-mode-login').classList.replace('text-brand-600', 'text-slate-500');
        document.getElementById('form-register').classList.remove('hidden');
        document.getElementById('form-login').classList.add('hidden');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    showLoader();
    try {
        const email = document.getElementById('login-email').value;
        const pass = document.getElementById('login-password').value;
        const resp = await API.login(email, pass);
        finishAuth(resp.accessToken, email);
    } catch (err) {
        hideLoader();
        showToast("Xəta!", err.message, true);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    showLoader();
    try {
        const user = {
            fullName: document.getElementById('reg-firstname').value + " " + document.getElementById('reg-lastname').value,
            email: document.getElementById('reg-email').value,
            phone: document.getElementById('reg-phone').value,
            password: document.getElementById('reg-password').value
        };
        const resp = await API.register(user);
        finishAuth(resp.accessToken, user.email);
    } catch(err) {
        hideLoader();
        showToast("Xəta!", err.message, true);
    }
}

function parseJwt (token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch (e) { return null; }
}

function finishAuth(token, email) {
    localStorage.setItem('salon_token', token);
    const decoded = parseJwt(token);
    // Spring Security roles usually have ROLE_ prefix
    const role = decoded.role || decoded.roles?.[0] || 'ROLE_USER'; 
    
    currentUser = { email: email || decoded.sub, role: role };
    document.getElementById('nav-username').textContent = email.split('@')[0];
    document.getElementById('nav-useremail').textContent = email;
    
    document.getElementById('nav-role-badge').textContent = role.replace('ROLE_', '');
    document.getElementById('shared-navbar').classList.remove('hidden');
    
    hideLoader();
    showToast("Uğurlu", "Sistemə daxil oldunuz!");
    
    // Switch completely based on ROLE
    document.querySelectorAll('.app-view').forEach(v => v.classList.remove('active'));
    
    if (role.includes('ADMIN')) {
        document.getElementById('view-admin').classList.add('active');
        adminLoadDashboard();
    } else if (role.includes('BARBER')) {
        document.getElementById('view-barber').classList.add('active');
        document.getElementById('barber-panel-name').textContent = currentUser.email.split('@')[0];
        barberLoadReservations();
    } else {
        document.getElementById('view-user').classList.add('active');
        userLoadSalons();
    }
}

function logout() {
    localStorage.removeItem('salon_token');
    currentUser = null;
    document.getElementById('shared-navbar').classList.add('hidden');
    document.querySelectorAll('.app-view').forEach(v => v.classList.remove('active'));
    document.getElementById('view-auth').classList.add('active');
}

// =======================
// ADMIN LOGIC
// =======================
function adminSwitchTab(tabId) {
    document.querySelectorAll('.admin-tab-content').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.admin-tab-link').forEach(el => {
        el.classList.remove('bg-brand-500', 'text-white', 'font-semibold');
        el.classList.add('hover:bg-slate-800', 'hover:text-white', 'font-medium');
    });
    
    document.getElementById(`admin-tab-${tabId}`).classList.add('active');
    
    const activeLink = document.querySelector(`.admin-tab-link[data-tab="${tabId}"]`);
    activeLink.classList.remove('hover:bg-slate-800', 'hover:text-white', 'font-medium');
    activeLink.classList.add('bg-brand-500', 'text-white', 'font-semibold');
}

async function adminLoadDashboard() {
    try {
        const [salons, barbers, reservations] = await Promise.all([
            API.fetchSalons(), API.fetchBarbers(), API.fetchAllReservations()
        ]);
        document.getElementById('admin-stat-salons').textContent = salons.length;
        document.getElementById('admin-stat-barbers').textContent = barbers.length;
        document.getElementById('admin-stat-services').textContent = reservations.length + " Rez";
        
        // Populate Salons
        const sBody = document.getElementById('admin-salons-tbody');
        sBody.innerHTML = salons.map(s => `<tr>
            <td class="p-4 font-bold text-slate-900">${s.name}</td>
            <td class="p-4">${s.address}</td>
            <td class="p-4">${s.isActive ? '<span class="text-emerald-500">Aktiv</span>' : '<span class="text-red-500">Passiv</span>'}</td>
            <td class="p-4 text-right"><button class="text-brand-600 hover:text-brand-800">Düzəlt <i class="fa-solid fa-pen ml-1"></i></button></td>
        </tr>`).join('');
        
        // Populate Barbers
        const bBody = document.getElementById('admin-barbers-tbody');
        bBody.innerHTML = barbers.map(b => `<tr>
            <td class="p-4 font-bold text-slate-900">${b.displayName || 'Bərbər '+b.id}</td>
            <td class="p-4">${b.salonName || b.salonId || '-'}</td>
            <td class="p-4">${b.isActive ? '<span class="text-emerald-500">Aktiv</span>' : '<span class="text-red-500">Passiv</span>'}</td>
            <td class="p-4 text-right"><button class="text-brand-600 hover:text-brand-800">Düzəlt <i class="fa-solid fa-pen ml-1"></i></button></td>
        </tr>`).join('');
    } catch(err) { console.error(err); }
}

// =======================
// USER LOGIC
// =======================
async function userLoadSalons() {
    try {
        showLoader();
        const salons = await API.fetchSalons();
        const grid = document.getElementById('user-salons-grid');
        grid.innerHTML = salons.map(s => `
            <div class="bg-white rounded-3xl p-6 border border-slate-200 shadow-sm hover:shadow-xl hover:-translate-y-1 transition duration-300">
                <div class="h-40 bg-gradient-to-tr from-slate-100 to-slate-200 rounded-2xl mb-6 flex items-center justify-center relative overflow-hidden">
                    <i class="fa-solid fa-store text-5xl text-slate-300"></i>
                    <div class="absolute top-4 right-4"><span class="bg-emerald-100 text-emerald-700 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide">AÇIQ</span></div>
                </div>
                <h3 class="text-xl font-extrabold text-slate-900 mb-2 truncate">${s.name}</h3>
                <div class="space-y-2 mb-6">
                    <p class="text-slate-500 text-sm truncate" title="${s.address}"><i class="fa-solid fa-location-dot w-5 text-center text-brand-500"></i> ${s.address}</p>
                    <p class="text-slate-500 text-sm"><i class="fa-solid fa-phone w-5 text-center text-brand-500"></i> ${s.contactPhone}</p>
                </div>
                <button onclick="openSalonDetails(${s.id}, '${s.name.replace(/'/g, "\\'")}')" class="w-full bg-slate-900 hover:bg-brand-600 text-white font-bold py-3.5 rounded-xl transition">Xidmətlərə Bax <i class="fa-solid fa-arrow-right ml-1"></i></button>
            </div>
        `).join('');
        hideLoader();
    } catch(err) {
        hideLoader();
        console.error(err);
        showToast("Xəta", "Salonlar yüklənə bilmədi. Backend API açıq deyil ola biler.", true);
    }
}

function backToSalons() {
    document.getElementById('user-salon-details-view').classList.add('hidden');
    document.getElementById('user-explore-view').classList.remove('hidden');
    document.getElementById('barber-services-section').classList.add('hidden');
    selectedServiceIds = [];
}

async function openSalonDetails(salonId, salonName) {
    try {
        showLoader();
        document.getElementById('salon-detail-name').textContent = salonName;
        selectedServiceIds = []; // clear previous selection
        selectedBarberId = null;

        const barbers = await API.fetchBarbersBySalon(salonId);
        
        document.getElementById('user-explore-view').classList.add('hidden');
        document.getElementById('user-salon-details-view').classList.remove('hidden');
        document.getElementById('barber-services-section').classList.add('hidden');
        
        const grid = document.getElementById('salon-barbers-grid');
        grid.innerHTML = barbers.map(b => `
            <div class="bg-white rounded-2xl p-5 border border-slate-200 shadow-sm hover:border-brand-300 hover:shadow-md transition cursor-pointer text-center barber-card" data-id="${b.id}" onclick="loadBarberServices(${b.id}, '${b.displayName.replace(/'/g, "\\'")}')">
                <div class="w-16 h-16 mx-auto rounded-full bg-brand-50 border-4 border-white shadow-sm flex items-center justify-center text-xl text-brand-600 mb-2">
                    <i class="fa-solid fa-user-tie"></i>
                </div>
                <h4 class="font-bold text-slate-800 text-sm truncate">${b.displayName}</h4>
                <p class="text-[10px] text-brand-600 font-bold mt-1 uppercase tracking-wider">Seç</p>
            </div>
        `).join('');
        
        hideLoader();
    } catch(err) {
        hideLoader();
        showToast("Xəta", "Bərbərlər tapılmadı.", true);
    }
}

async function loadBarberServices(barberId, barberName) {
    try {
        showLoader();
        selectedBarberId = barberId;
        selectedBarberName = barberName;
        selectedServiceIds = []; // reset services on barber switch
        
        document.getElementById('selected-barber-name').textContent = barberName + " - Təklif etdiyi Xidmətlər";
        
        // UI highlight selected barber
        document.querySelectorAll('.barber-card').forEach(el => el.classList.remove('border-brand-500', 'bg-brand-50'));
        const card = document.querySelector(`.barber-card[data-id="${barberId}"]`);
        if(card) card.classList.add('border-brand-500', 'bg-brand-50');

        const services = await API.fetchBarberServices(barberId);
        const sec = document.getElementById('barber-services-section');
        const list = document.getElementById('barber-services-list');
        
        sec.classList.remove('hidden');
        
        if (services.length === 0) {
            list.innerHTML = '<p class="text-slate-500 font-medium p-4">Bu bərbərin hazırda xidməti yoxdur.</p>';
        } else {
            list.innerHTML = services.map(s => `
                <div id="srv-${s.id}" class="service-item flex items-center justify-between p-4 rounded-2xl border border-slate-100 bg-slate-50 transition cursor-pointer hover:border-brand-300" onclick="toggleService(${s.id}, ${s.price})">
                    <div class="flex items-center gap-4">
                        <div class="checkbox-box w-6 h-6 rounded-lg border-2 border-slate-300 flex items-center justify-center transition">
                            <i class="fa-solid fa-check text-xs text-white opacity-0"></i>
                        </div>
                        <div>
                            <h4 class="font-bold text-slate-900">${s.name}</h4>
                            <p class="text-xs text-slate-500 mt-0.5"><i class="fa-regular fa-clock"></i> ${s.durationMin} dəq.</p>
                        </div>
                    </div>
                    <div class="font-extrabold text-slate-900 text-lg">${s.price} ₼</div>
                </div>
            `).join('');
            
            // Add summary bar at the bottom of section
            list.innerHTML += `
               <div id="service-basket-bar" class="hidden mt-8 p-6 bg-brand-600 rounded-2xl text-white flex justify-between items-center shadow-xl shadow-brand-500/30">
                   <div>
                       <p class="text-xs font-bold text-brand-100 uppercase tracking-widest uppercase">Seçilmiş Xidmətlər</p>
                       <p class="text-2xl font-extrabold" id="total-price-basket">0.00 ₼</p>
                   </div>
                   <button onclick="proceedToReservation()" class="bg-white text-brand-600 px-6 py-3 rounded-xl font-bold text-base hover:bg-brand-50 transition transform active:scale-95 shadow-lg">Təqvimi Aç <i class="fa-solid fa-arrow-right ml-2"></i></button>
               </div>
            `;
        }
        
        setTimeout(() => sec.scrollIntoView({ behavior: 'smooth', block: 'nearest' }), 100);
        hideLoader();
    } catch(err) {
        hideLoader();
        showToast("Xəta", "Xidmətlər yüklənmədi.", true);
    }
}

let totalPrice = 0;
function toggleService(id, price) {
    const srv = document.getElementById(`srv-${id}`);
    const check = srv.querySelector('.checkbox-box');
    const icon = srv.querySelector('.fa-check');
    
    if (selectedServiceIds.includes(id)) {
        selectedServiceIds = selectedServiceIds.filter(sid => sid !== id);
        srv.classList.remove('border-brand-500', 'bg-brand-50');
        check.classList.remove('bg-brand-500', 'border-brand-500');
        icon.classList.add('opacity-0');
        totalPrice -= price;
    } else {
        selectedServiceIds.push(id);
        srv.classList.add('border-brand-500', 'bg-brand-50');
        check.classList.add('bg-brand-500', 'border-brand-500');
        icon.classList.remove('opacity-0');
        totalPrice += price;
    }
    
    const bar = document.getElementById('service-basket-bar');
    if (selectedServiceIds.length > 0) {
        bar.classList.remove('hidden');
        document.getElementById('total-price-basket').textContent = totalPrice.toFixed(2) + " ₼";
    } else {
        bar.classList.add('hidden');
    }
}

let selectedSlot = null;

function proceedToReservation() {
    document.getElementById('reservation-modal').classList.remove('hidden');
    // set default date to today
    const now = new Date();
    const dateInput = document.getElementById('res-date-input');
    dateInput.min = now.toISOString().split('T')[0];
    dateInput.value = dateInput.min;
    updateSlots();
}

function closeReservationModal() {
    document.getElementById('reservation-modal').classList.add('hidden');
    selectedSlot = null;
}

async function updateSlots() {
    const date = document.getElementById('res-date-input').value;
    const grid = document.getElementById('slots-grid');
    const btn = document.getElementById('btn-confirm-booking');
    
    if(!date) return;
    
    try {
        grid.innerHTML = '<div class="col-span-3 text-center py-4 text-slate-400 font-medium"><i class="fa-solid fa-spinner fa-spin mr-2"></i> Saatlar yoxlanılır...</div>';
        btn.disabled = true;
        
        // Custom simple logic: we fetch from backend if available, or generate 09:00-21:00
        // Our backend has fetchAvailableSlots
        const slots = await API.fetchAvailableSlots(selectedBarberId, date);
        
        if (slots.length === 0) {
            grid.innerHTML = '<div class="col-span-3 text-center py-4 text-red-400 font-medium">Bu tarixdə boş vaxt yoxdur.</div>';
        } else {
            grid.innerHTML = slots.map(time => `
                <button onclick="selectSlot('${time}')" class="slot-btn py-3 rounded-xl border border-slate-200 font-bold text-slate-700 hover:border-brand-500 transition" data-time="${time}">${time}</button>
            `).join('');
        }
    } catch(err) {
        grid.innerHTML = '<div class="col-span-3 text-center py-4 text-red-500">Xəta baş verdi.</div>';
    }
}

function selectSlot(time) {
    selectedSlot = time;
    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('bg-brand-600', 'text-white', 'border-brand-600'));
    const btn = document.querySelector(`.slot-btn[data-time="${time}"]`);
    if(btn) btn.classList.add('bg-brand-600', 'text-white', 'border-brand-600');
    document.getElementById('btn-confirm-booking').disabled = false;
}

async function finalizeReservation() {
    if(!selectedBarberId || selectedServiceIds.length === 0 || !selectedSlot) return;
    
    try {
        showLoader();
        const date = document.getElementById('res-date-input').value;
        
        const payload = {
            barberId: selectedBarberId,
            serviceIds: selectedServiceIds,
            date: date,
            startTime: selectedSlot // format: "HH:mm" (Spring LocalTime handles it)
        };
        
        await API.createReservation(payload);
        
        hideLoader();
        closeReservationModal();
        showToast("Uğurlu!", "Rezervasiyanız yaradıldı. Admin tərəfindən təsdiq gözləyir.", false);
        
        // Reset and go back
        backToSalons();
    } catch(err) {
        hideLoader();
        showToast("Xəta", err.message, true);
    }
}

// =======================
// BARBER LOGIC
// =======================
async function barberLoadReservations() {
    try {
        showLoader();
        const resList = await API.fetchBarberSchedule();
        document.getElementById('barber-stat-total').textContent = resList.length;
        
        const body = document.getElementById('barber-reservations-tbody');
        const empty = document.getElementById('barber-res-empty');
        
        if (resList.length === 0) {
            body.innerHTML = '';
            empty.classList.remove('hidden');
        } else {
            empty.classList.add('hidden');
            body.innerHTML = resList.map(r => `
                <tr class="hover:bg-brand-50 transition">
                    <td class="p-4">${new Date(r.startAt).toLocaleString('az-AZ')}</td>
                    <td class="p-4 font-bold text-slate-900">${r.customerName || 'Müştəri '+r.customerId}</td>
                    <td class="p-4">${r.serviceName || '-'}</td>
                    <td class="p-4"><span class="bg-brand-100 text-brand-700 px-2.5 py-1 rounded-lg text-xs font-bold">${r.status || 'TƏSDİQLƏNİB'}</span></td>
                </tr>
            `).join('');
        }
        hideLoader();
    } catch(err) {
        hideLoader();
        console.error(err);
        showToast("Cədvəl tapılmadı", "API-ə qoşulmaq alınmadı.", true);
    }
}

// Check initial mount
window.onload = () => {
    const token = localStorage.getItem('salon_token');
    if (token) {
        // If token exists, auto login visually to role panel
        finishAuth(token, parseJwt(token).sub || 'User');
    }
};
