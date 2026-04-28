package com.carpool.model.entities;

import com.carpool.model.enums.Role;

public class Admin extends User {
    public Admin(String id, String nom, String prenom, String email, String password) {
        super(id, nom, prenom, email, password, Role.ADMIN);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
