package com.carpool.model.entities;

import com.carpool.model.enums.Role;
import com.carpool.model.enums.UserStatus;
import java.util.Objects;

public abstract class User {
    protected String id;
    protected String nom;
    protected String prenom;
    protected String email;
    protected String password;
    protected Role role;
    
    // Security fields
    protected UserStatus status;
    protected int failedLoginAttempts;

    public User(String id, String nom, String prenom, String email, String password, Role role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = UserStatus.ACTIF;
        this.failedLoginAttempts = 0;
    }

    public void loadSecurityState(UserStatus status, int attempts) {
        this.status = status;
        this.failedLoginAttempts = attempts;
    }

    public void receiveNotification(String message, String type) {
        // Validation Règle 1: "Notification par email et SMS intégrée dans les objets utilisateurs"
        String template = "[%s -> %s %s (%s)]: %s";
        System.out.println(String.format(template, "EMAIL " + type, this.nom, this.prenom, this.email, message));
        System.out.println(String.format(template, "SMS " + type, this.nom, this.prenom, this.email, message));
    }

    // Business Logic Validation for Security
    // Validation Règle 4: "Certaines méthodes doivent être private ou protected / Les objets ne modifient que leur état"
    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.status = UserStatus.BLOQUE;
        }
    }

    public void resetFailedLogins() {
        this.failedLoginAttempts = 0;
    }

    public void suspendAccount() {
        this.status = UserStatus.SUSPENDU;
    }

    protected boolean isLocked() {
        return this.status == UserStatus.BLOQUE || this.status == UserStatus.SUSPENDU;
    }

    // Basic Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
