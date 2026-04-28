package com.carpool.model.entities;

import com.carpool.model.enums.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chauffeur extends User {
    private List<Vehicule> vehicules;
    private double rating;
    private int avisCount;

    public Chauffeur(String id, String nom, String prenom, String email, String password) {
        super(id, nom, prenom, email, password, Role.CHAUFFEUR);
        this.vehicules = new ArrayList<>();
        this.rating = 0.0;
        this.avisCount = 0;
    }

    public void addVehicule(Vehicule vehicule) {
        this.vehicules.add(vehicule);
    }

    public List<Vehicule> getVehicules() {
        return Collections.unmodifiableList(vehicules);
    }

    public void addAvis(double newRating) {
        this.rating = ((this.rating * this.avisCount) + newRating) / (++this.avisCount);
    }

    public double getRating() { return rating; }

    @Override
    public String toString() {
        return "Chauffeur{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", rating=" + rating +
                '}';
    }
}
