package com.carpool;

import com.carpool.model.entities.*;
import com.carpool.service.*;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Initialisation du Backend NexRide ===");
        
        NotificationService notificationService = new NotificationService();
        AuthService authService = new AuthService();
        PaiementService paiementService = new PaiementService(notificationService);
        TrajetService trajetService = new TrajetService();
        ReservationService reservationService = new ReservationService(paiementService, notificationService, trajetService, authService);
        
        System.out.println("\n[1] Inscription des utilisateurs...");
        Chauffeur chauffeur = new Chauffeur("C1", "Dupont", "Marc", "marc@test.com", "pass123");
        Passager passager = new Passager("P1", "Martin", "Sophie", "sophie@test.com", "pass456");
        passager.addMoyenPaiement(new MoyenPaiement("MP1", "P1", "CB", "1234"));
        
        authService.registerUser(chauffeur);
        authService.registerUser(passager);
        System.out.println("Utilisateurs enregistrés avec succès.");
        
        System.out.println("\n[2] Création d'un Trajet par le Chauffeur...");
        Trajet trajet = new Trajet("T1", chauffeur.getId(), "Paris", "Lyon", LocalDateTime.now().plusDays(2), 3, 35.0);
        trajetService.addTrajet(trajet);
        System.out.println("Trajet créé: " + trajet);
        
        System.out.println("\n[3] Sophie réserve 1 place...");
        Reservation res = new Reservation("R1", trajet.getId(), passager.getId(), 1, trajet.getPrixParPlace());
        reservationService.creerReservation(res);
        System.out.println("Etat du trajet mis à jour: " + trajet.getPlacesDisponibles() + " places libres.");
        
        System.out.println("\n[4] Marc accepte la réservation...");
        reservationService.accepterReservation(res.getId());
        
        System.out.println("\n[5] Sophie annule après acceptation (> 24h avant départ)...");
        reservationService.annulerParPassager(res.getId());
        
        System.out.println("\n=== Traitement Terminé ===");
    }
}
