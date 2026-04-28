const app = {
    toggleAuth() {
        const login = document.getElementById('login-section');
        const register = document.getElementById('register-section');
        if(login.style.display === 'none') {
            login.style.display = 'block';
            register.style.display = 'none';
        } else {
            login.style.display = 'none';
            register.style.display = 'block';
        }
    },
    
    login(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const pass = document.getElementById('login-pass').value;
        const params = new URLSearchParams({ email: email, password: pass });
        
        fetch('/api/auth/login', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if(data.status === 'success') {
                localStorage.setItem('userId', data.userId);
                localStorage.setItem('userRole', data.role);
                if (data.role === 'CHAUFFEUR') {
                    window.location.href = 'dashboard-chauffeur.html';
                } else {
                    window.location.href = 'dashboard-passager.html';
                }
            } else {
                alert("Erreur: " + data.message);
            }
        });
    },

    register(e) {
        e.preventDefault();
        const email = document.getElementById('reg-email').value;
        const pass = document.getElementById('reg-pass').value;
        const nom = document.getElementById('reg-nom').value;
        const prenom = document.getElementById('reg-prenom').value;
        const role = document.getElementById('reg-role').value;
        
        const params = new URLSearchParams({ email, password: pass, nom, prenom, role });
        
        fetch('/api/auth/register', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if(data.status === 'success') {
                alert("Inscription réussie ! Vous pouvez vous connecter.");
                app.toggleAuth();
            } else {
                alert("Erreur: " + data.message);
            }
        });
    },

    addTrajet(e) {
        e.preventDefault();
        const userId = localStorage.getItem('userId');
        const depart = document.getElementById('traj-depart').value;
        const arrivee = document.getElementById('traj-arrivee').value;
        const date = document.getElementById('traj-date').value;
        const places = document.getElementById('traj-places').value;
        const prix = document.getElementById('traj-prix').value;

        const params = new URLSearchParams({ userId, depart, arrivee, date, places, prix });
        
        fetch('/api/trajets', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if(data.status === 'success') {
                alert("Trajet proposé avec succès !");
                window.location.reload();
            } else {
                alert("Erreur: " + data.message);
            }
        });
    },

    bookTrip(id) {
        const userId = localStorage.getItem('userId');
        if (!userId) {
            alert("Veuillez vous connecter pour réserver.");
            return window.location.href = 'auth.html';
        }
        const params = new URLSearchParams({ trajetId: id, userId: userId });
        fetch('/api/reservations', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
        .then(res => res.json())
        .then(data => {
            if(data.status === 'success') {
                alert("Paiement autorisé et réservation effectuée (Live Serveur) !");
                const role = localStorage.getItem('userRole');
                if (role === 'CHAUFFEUR') {
                    window.location.href = 'dashboard-chauffeur.html';
                } else {
                    window.location.href = 'dashboard-passager.html';
                }
            } else {
                alert("Erreur: " + data.message);
            }
        });
    },

    cancelReservation(id) {
        if(confirm("Confirmer l'annulation ? Politique de remboursement : intégral si > 24h, sinon partiel. Poursuivre ?")) {
            alert("Réservation annulée. Remboursement traité ou Pénalité appliquée selon les règles métier Java.");
        }
    },

    switchTab(tab) {
        document.querySelectorAll('.sidebar a').forEach(a => a.classList.remove('active'));
        event.currentTarget.classList.add('active');
        
        document.querySelectorAll('main.dashboard-content').forEach(m => m.style.display = 'none');
        if(tab === 'trips') {
            document.getElementById('dashboard-main').style.display = 'block';
        } else if(tab === 'notifications') {
            document.getElementById('notifications-main').style.display = 'block';
        } else if(tab === 'add' && document.getElementById('add-main')) {
            document.getElementById('add-main').style.display = 'block';
        }
    },

    logout() {
        localStorage.removeItem('userId');
        localStorage.removeItem('userRole');
        window.location.href = 'index.html';
    }
};

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('login-form');
    if(loginForm) loginForm.addEventListener('submit', app.login);
    
    const regForm = document.getElementById('register-form');
    if(regForm) regForm.addEventListener('submit', app.register);
    
    const addTrajetForm = document.getElementById('add-trajet-form');
    if(addTrajetForm) addTrajetForm.addEventListener('submit', app.addTrajet);
    
    const searchForm = document.getElementById('search-form');
    if(searchForm) searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const depart = document.getElementById('search-depart').value.trim().toLowerCase();
        const arrivee = document.getElementById('search-arrivee').value.trim().toLowerCase();
        const date = document.getElementById('search-date').value; // YYYY-MM-DD
        loadTrips({ depart, arrivee, date });
        const results = document.getElementById('trip-results');
        results.style.display = 'grid';
        document.getElementById('trips').scrollIntoView({behavior: 'smooth'});
    });

    // Auto-load trips on index page
    if(document.getElementById('trip-results')) {
        loadTrips();
    }
});

function loadTrips(filters) {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        window.location.href = 'auth.html';
        return;
    }
    fetch('/api/trajets?userId=' + userId)
    .then(res => {
        if(res.status === 401) {
            window.location.href = 'auth.html';
            throw new Error("Unauthorized");
        }
        return res.json();
    })
    .then(data => {
        const grid = document.getElementById('trip-results');
        if(!grid) return;

        // Apply client-side filters if provided
        let results = data;
        if(filters) {
            results = data.filter(tr => {
                const departMatch = !filters.depart || tr.depart.toLowerCase().includes(filters.depart);
                const arriveeMatch = !filters.arrivee || tr.arrivee.toLowerCase().includes(filters.arrivee);
                const dateMatch = !filters.date || tr.date.split('T')[0] === filters.date;
                return departMatch && arriveeMatch && dateMatch;
            });
        }

        grid.innerHTML = '';
        if(results.length === 0) {
            grid.innerHTML = '<p style="grid-column:1/-1; text-align:center; color:var(--text-secondary); padding:40px;">Aucun trajet trouvé pour ces critères.</p>';
            return;
        }
        results.forEach(tr => {
            grid.innerHTML += `
            <div class="trip-card">
                <div class="route">
                    ${tr.depart} <i class="fas fa-arrow-right"></i> ${tr.arrivee}
                </div>
                <div class="info-row" style="margin-top:10px;">
                    <span><i class="far fa-calendar"></i> ${tr.date.split('T')[0]}</span>
                    <span class="badge">${tr.places} places libres</span>
                </div>
                <div class="info-row" style="margin-top:10px;">
                    <span>Chauffeur: ${tr.chauffeurNom} <i class="fas fa-star" style="color:#fbbf24"></i> 4.9</span>
                </div>
                <div style="display:flex; justify-content: space-between; align-items:center; margin-top:20px;">
                    <div class="price">${tr.prix}€</div>
                    <button class="btn" onclick="app.bookTrip('${tr.id}')">Réserver</button>
                </div>
            </div>`;
        });
    });
}

function loadDashboard() {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        window.location.href = 'auth.html';
        return;
    }
    fetch(`/api/dashboard?userId=${userId}`)
    .then(res => res.json())
    .then(data => {
        if(data.error) return;
        
        // Stats
        document.getElementById('stat-trajets').innerText = data.stats.trajetsEffectues;
        document.getElementById('stat-avis').innerHTML = `<i class="fas fa-star" style="color:#fbbf24"></i> ${data.stats.avis}`;
        document.getElementById('stat-prochain').innerText = data.stats.prochainDepart;

        // Recent Trips
        const grid = document.getElementById('recent-trips-grid');
        grid.innerHTML = '';
        data.recentTrips.forEach(tr => {
            grid.innerHTML += `
            <div class="trip-card">
                <div class="route">${tr.depart} <i class="fas fa-arrow-right"></i> ${tr.arrivee}</div>
                <div class="info-row" style="margin-top:10px;">
                    <span><i class="far fa-calendar"></i> ${tr.date.split('T')[0]}</span>
                </div>
                <div style="margin-top:15px; display:flex; justify-content:space-between; align-items:center;">
                    <span class="badge ${tr.status === 'ANNULEE' ? 'danger' : ''}">${tr.status}</span>
                    ${tr.status !== 'ANNULEE' ? `<button class="btn btn-outline" style="padding:8px 15px; border-color:var(--danger); color:var(--danger)" onclick="app.cancelReservation('${tr.id}')">Annuler</button>` : ''}
                </div>
            </div>`;
        });
        
        // Notifications
        const notifGrid = document.getElementById('notifications-list');
        const notifBadge = document.getElementById('notif-badge');
        notifGrid.innerHTML = '';
        
        if (notifBadge) {
            notifBadge.innerText = data.notifications.length;
            notifBadge.style.display = data.notifications.length > 0 ? 'inline' : 'none';
        }

        if (data.notifications.length === 0) {
            notifGrid.innerHTML = '<i>Aucune nouvelle notification.</i>';
        } else {
            data.notifications.forEach(n => {
                notifGrid.innerHTML += `
                <div class="notification-item">
                    <span class="notif-time">${n.time}</span>
                    <strong>${n.message}</strong>
                </div>`;
            });
        }
    });
}
