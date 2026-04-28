# Backend Endpoints Architecture

To securely bridge the Java services (Phase 1) with our Web 2.0 Frontend (Phase 2), we'll expose a set of RESTful APIs. 

## 1. Authentication (Connexion & Inscription)
* **POST /api/auth/register**
    - Body: `{ email, password, nom, prenom, role }`
    - Response: `201 Created` / User details
* **POST /api/auth/login**
    - Body: `{ email, password }`
    - Response: `200 OK` / JWT Token & User details

## 2. Trip Management (Consulter et gérer les trajets)
* **GET /api/trajets**
    - Query: `?depart=ville&arrivee=ville&date=YYYY-MM-DD`
    - Response: `200 OK` / Array of `Trajet`
* **POST /api/trajets** *(Driver Only)*
    - Body: `{ pointDepart, pointArrivee, dateHeure, placesDisponibles, prixParPlace }`
    - Response: `201 Created` / Created Trip
* **DELETE /api/trajets/{id}** *(Driver Only)*
    - Triggers driver cancellation + 20% penalty if under 24h.

## 3. Reservations & Payments
* **POST /api/reservations** *(Passenger Only)*
    - Body: `{ trajetId, nbPlaces }`
    - Logic: Triggers immediate payment auth. Triggers trip status COMPLET if full.
* **POST /api/reservations/{id}/accept** *(Driver Only)*
    - Logic: Triggers payment capture.
* **DELETE /api/reservations/{id}** *(Passenger Only)*
    - Logic: Triggers Passenger cancellation policy (full or partial refund based on 24h limit).

## 4. Notifications
* **GET /api/users/{id}/notifications**
    - Retrieve inbox of system notifications sent to the user (e.g. from PaiementService or TrajetService).
