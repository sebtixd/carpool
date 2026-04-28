package com.carpool.service;

import com.carpool.model.entities.Trajet;
import com.carpool.model.enums.TrajetStatus;
import com.carpool.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrajetService {
    public void addTrajet(Trajet trajet) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO TRAJETS VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, trajet.getId());
            ps.setString(2, trajet.getChauffeurId());
            ps.setString(3, trajet.getPointDepart());
            ps.setString(4, trajet.getPointArrivee());
            ps.setString(5, trajet.getDateHeure().toString());
            ps.setInt(6, trajet.getPlacesDisponibles());
            ps.setDouble(7, trajet.getPrixParPlace());
            ps.setString(8, trajet.getStatus().name());
            ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTrajet(Trajet trajet) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE TRAJETS SET placesDisponibles=?, status=? WHERE id=?");
            ps.setInt(1, trajet.getPlacesDisponibles());
            ps.setString(2, trajet.getStatus().name());
            ps.setString(3, trajet.getId());
            ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public Trajet getTrajet(String id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TRAJETS WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapToTrajet(rs);
        } catch(Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public List<Trajet> getAllTrajets() {
        List<Trajet> trips = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TRAJETS");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                trips.add(mapToTrajet(rs));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return trips;
    }

    public List<Trajet> getTrajetsForChauffeur(String chauffeurId) {
        List<Trajet> trips = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM TRAJETS WHERE chauffeurId=?");
            ps.setString(1, chauffeurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                trips.add(mapToTrajet(rs));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return trips;
    }
    
    private Trajet mapToTrajet(ResultSet rs) throws Exception {
        Trajet t = new Trajet(
            rs.getString("id"),
            rs.getString("chauffeurId"),
            rs.getString("pointDepart"),
            rs.getString("pointArrivee"),
            LocalDateTime.parse(rs.getString("dateHeure")),
            rs.getInt("placesDisponibles"),
            rs.getDouble("prixParPlace")
        );
        t.setStatus(TrajetStatus.valueOf(rs.getString("status")));
        return t;
    }
}
