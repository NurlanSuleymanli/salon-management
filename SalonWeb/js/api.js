// API Configuration & Networking Layer
const BASE_URL = 'http://localhost:8080/api';
const AUTH_URL = 'http://localhost:8080/auth';

const API = {
    // ---------------- AUTH ----------------
    async login(email, password) {
        const res = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        if (!res.ok) throw new Error("Email və ya şifrə yanlışdır.");
        return await res.json();
    },

    async register(user) {
        const res = await fetch(`${AUTH_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(user)
        });
        if (!res.ok) throw new Error("Qeydiyyat xətası! Məlumatları düzgün yoxlayın.");
        return await res.json(); // returns token
    },

    // ---------------- TOKEN UTIL ----------------
    authHeaders() {
        const token = localStorage.getItem('salon_token');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    },

    // ---------------- FETCHERS ----------------
    async fetchSalons() {
        const res = await fetch(`${BASE_URL}/salons/list?size=20`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Salonlar çəkilə bilmədi");
        const data = await res.json();
        return data.content || data;
    },

    async fetchBarbers() {
        const res = await fetch(`${BASE_URL}/barbers/list?size=30`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Bərbərlər çəkilə bilmədi");
        const data = await res.json();
        return data.content || data;
    },

    // New Endpoint: Filter Barbers by Service Idea
    async filterBarbersByService(serviceId) {
        const res = await fetch(`${BASE_URL}/barbers/filter?serviceId=${serviceId}`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Filter işləmədi");
        return await res.json();
    },

    async fetchBarbersBySalon(salonId) {
        const res = await fetch(`${BASE_URL}/barbers/salon/${salonId}`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Bu salona aid bərbərlər tapılmadı");
        return await res.json();
    },

    async fetchBarberServices(barberId) {
        const res = await fetch(`${BASE_URL}/barbers/${barberId}/services`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Bərbərin xidmətləri oxunmadı");
        return await res.json();
    },

    async fetchAvailableSlots(barberId, dateString) {
        const res = await fetch(`${BASE_URL}/barbers/${barberId}/available-slots?date=${dateString}`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Boş saatlar tapılmadı");
        return await res.json();
    },

    // Admin Specific
    async fetchAllReservations() {
        const res = await fetch(`${BASE_URL}/reservations/all`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Rezervasiya tarixçəsi oxunmadı");
        const data = await res.json();
        return data.content || data;
    },

    // Barber Specific
    async fetchBarberSchedule() {
        const res = await fetch(`${BASE_URL}/reservations/barber-schedule`, { headers: this.authHeaders() });
        if (!res.ok) throw new Error("Cədvəl tapılmadı");
        const data = await res.json();
        return data.content || data;
    },

    async createReservation(reservationData) {
        const res = await fetch(`${BASE_URL}/reservations/create`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                ...this.authHeaders()
            },
            body: JSON.stringify(reservationData)
        });
        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || "Rezervasiya xətası!");
        }
        return await res.json();
    }
};
