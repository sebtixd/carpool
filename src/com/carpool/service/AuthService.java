package com.carpool.service;

import com.carpool.model.entities.User;
import com.carpool.model.entities.Passager;
import com.carpool.model.entities.Chauffeur;
import com.carpool.model.entities.Admin;
import com.carpool.model.enums.Role;
import com.carpool.model.enums.UserStatus;
import com.carpool.exception.CarpoolException;
import com.carpool.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    public void registerUser(User user) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO USERS (id, nom, prenom, email, password, role, status, failedLoginAttempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, user.getId());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRole().name());
            ps.setString(7, user.getStatus().name());
            ps.setInt(8, user.getFailedLoginAttempts());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new CarpoolException("Erreur: Un utilisateur avec cet email exists déjà.");
        }
    }

    public User login(String email, String password) {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new CarpoolException("Compte introuvable.");
        }
        
        // Security Rules Checking
        if (user.getStatus() == UserStatus.BLOQUE || user.getStatus() == UserStatus.SUSPENDU) {
            throw new CarpoolException("Votre compte est " + user.getStatus().name() + ". Accès refusé.");
        }

        if (!user.getPassword().equals(password)) {
            // Failed connection counter logic isolated inside User object
            user.recordFailedLogin();
            updateUserSecurityState(user);
            throw new CarpoolException("Identifiants incorrects. Tentatives échouées : " + user.getFailedLoginAttempts());
        }

        // Success - clean state
        user.resetFailedLogins();
        updateUserSecurityState(user);
        return user;
    }

    private void updateUserSecurityState(User user) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE USERS SET status=?, failedLoginAttempts=? WHERE id=?");
            ps.setString(1, user.getStatus().name());
            ps.setInt(2, user.getFailedLoginAttempts());
            ps.setString(3, user.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Validation Règle 1: "Suspension possible (ADMIN)"
    public void suspendUserByAdmin(User adminUser, String targetUserId) {
        if (adminUser == null || adminUser.getRole() != Role.ADMIN) {
            throw new CarpoolException("Non autorisé: Seuls les ADMIN peuvent suspendre des comptes.");
        }
        User target = getUserById(targetUserId);
        if (target != null) {
            target.suspendAccount();
            updateUserSecurityState(target);
        }
    }

    public User getUserByEmail(String email) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM USERS WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public User getUserById(String id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM USERS WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSetToUser(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws Exception {
        String id = rs.getString("id");
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String email = rs.getString("email");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        
        User loadedUser;
        switch (role) {
            case CHAUFFEUR: loadedUser = new Chauffeur(id, nom, prenom, email, password); break;
            case PASSAGER: 
                loadedUser = new Passager(id, nom, prenom, email, password); 
                ((Passager)loadedUser).addMoyenPaiement(new com.carpool.model.entities.MoyenPaiement("MP"+id, "CARTE_DEMO", "XXXX XXXX XXXX 0000", "01/99"));
                break;
            case ADMIN: loadedUser = new Admin(id, nom, prenom, email, password); break;
            default: return null;
        }

        // Set encapsulated security fields after init
        loadedUser.loadSecurityState(
            UserStatus.valueOf(rs.getString("status")), 
            rs.getInt("failedLoginAttempts")
        );
        return loadedUser;
    }
}
