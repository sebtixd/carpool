package com.carpool.service;

import com.carpool.model.entities.Reservation;
import com.carpool.model.entities.Trajet;
import com.carpool.model.entities.Passager;
import com.carpool.model.entities.Chauffeur;
import com.carpool.model.entities.User;
import com.carpool.model.enums.ReservationStatus;
import com.carpool.model.enums.TrajetStatus;
import com.carpool.exception.CarpoolException;
import com.carpool.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private PaiementService paiementService;
    private NotificationService notificationService;
    private TrajetService trajetService;
    private AuthService authService;

    public ReservationService(PaiementService paiementService, NotificationService notificationService, TrajetService trajetService, AuthService authService) {
        this.paiementService = paiementService;
        this.notificationService = notificationService;
        this.trajetService = trajetService;
        this.authService = authService;
    }

    public void creerReservation(Reservation reservation) {
        Trajet trajet = trajetService.getTrajet(reservation.getTrajetId());
        if (trajet == null) throw new CarpoolException("Trajet introuvable");
        
        Passager passager = (Passager) authService.getUserById(reservation.getPassagerId());
        paiementService.authorizePayment(passager, reservation);
        
        trajet.reserverPlaces(reservation.getNbPlaces());
        
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO RESERVATIONS VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, reservation.getId());
            ps.setString(2, reservation.getTrajetId());
            ps.setString(3, reservation.getPassagerId());
            ps.setInt(4, reservation.getNbPlaces());
            ps.setDouble(5, reservation.getTotalPrix());
            ps.setString(6, reservation.getStatus().name());
            ps.executeUpdate();
        } catch(Exception e) { e.printStackTrace(); }
        
        // Push state changes
        trajetService.updateTrajet(trajet);
        
        User chauffeur = authService.getUserById(trajet.getChauffeurId());
        if(chauffeur != null && passager != null) {
            String msg = String.format("Réservation par %s %s. Places restantes : %d", 
                passager.getPrenom(), passager.getNom(), trajet.getPlacesDisponibles());
            notificationService.sendNotification(chauffeur, msg, "RESERVATION_NEW");
        }
    }

    public void accepterReservation(String idReservation) {
        Reservation res = getReservation(idReservation);
        if(res == null) return;
        res.setStatus(ReservationStatus.ACCEPTEE);
        
        paiementService.capturePayment(res); // logic expects Auth boolean which we'd normally store
        updateReservationStatus(res);
        
        Passager passager = (Passager) authService.getUserById(res.getPassagerId());
        notificationService.sendNotification(passager, "Votre réservation a été acceptée", "RESERVATION_ACCEPTED");
    }

    public void annulerParPassager(String idReservation) {
        Reservation res = getReservation(idReservation);
        if(res == null) return;
        Trajet trajet = trajetService.getTrajet(res.getTrajetId());
        
        res.setStatus(ReservationStatus.ANNULEE);
        updateReservationStatus(res);
        
        trajet.annulerReservation(res.getNbPlaces());
        trajetService.updateTrajet(trajet);

        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), trajet.getDateHeure());
        Passager passager = (Passager) authService.getUserById(res.getPassagerId());

        if (hours > 24) {
            paiementService.processRefund(passager, res.getTotalPrix());
        } else {
            paiementService.processRefund(passager, res.getTotalPrix() * 0.5);
        }
        notificationService.sendNotification(passager, "Réservation annulée.", "RESERVATION_CANCELLED");
    }

    public void annulerParChauffeur(String trajetId) {
        Trajet trajet = trajetService.getTrajet(trajetId);
        Chauffeur chauffeur = (Chauffeur) authService.getUserById(trajet.getChauffeurId());
        
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), trajet.getDateHeure());
        
        List<Reservation> trajetReservations = getReservationsForTrajet(trajetId);

        for(Reservation res : trajetReservations) {
            if(res.getStatus() == ReservationStatus.ANNULEE) continue;
            
            res.setStatus(ReservationStatus.ANNULEE);
            updateReservationStatus(res);
            
            Passager passager = (Passager) authService.getUserById(res.getPassagerId());
            paiementService.processRefund(passager, res.getTotalPrix());
            notificationService.sendNotification(passager, "Le chauffeur a annulé le trajet.", "TRAJET_CANCELLED");
            
            if (hours < 24) {
                double penalty = res.getTotalPrix() * 0.2;
                paiementService.applyPenalty(chauffeur, penalty);
            }
        }
        trajet.setStatus(TrajetStatus.ANNULE);
        trajetService.updateTrajet(trajet);
    }
    
    private void updateReservationStatus(Reservation res) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE RESERVATIONS SET status=? WHERE id=?");
            ps.setString(1, res.getStatus().name());
            ps.setString(2, res.getId());
            ps.executeUpdate();
        } catch(Exception e) { e.printStackTrace(); }
    }
    
    private Reservation getReservation(String id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM RESERVATIONS WHERE id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return mapRes(rs);
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
    
    private List<Reservation> getReservationsForTrajet(String trajetId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM RESERVATIONS WHERE trajetId=?");
            ps.setString(1, trajetId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(mapRes(rs));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
    
    private Reservation mapRes(ResultSet rs) throws Exception {
        return new Reservation(
            rs.getString("id"),
            rs.getString("trajetId"),
            rs.getString("passagerId"),
            rs.getInt("nbPlaces"),
            rs.getDouble("totalPrix"),
            ReservationStatus.valueOf(rs.getString("status"))
        );
    }

    public List<Reservation> getReservationsForUser(String passagerId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM RESERVATIONS WHERE passagerId=?");
            ps.setString(1, passagerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(mapRes(rs));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
}
