package com.carpool.model.entities;

import com.carpool.model.enums.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Passager extends User {
    private List<MoyenPaiement> moyensPaiement;

    public Passager(String id, String nom, String prenom, String email, String password) {
        super(id, nom, prenom, email, password, Role.PASSAGER);
        this.moyensPaiement = new ArrayList<>();
    }

    public void addMoyenPaiement(MoyenPaiement moyenPaiement) {
        this.moyensPaiement.add(moyenPaiement);
    }

    public List<MoyenPaiement> getMoyensPaiement() {
        return Collections.unmodifiableList(moyensPaiement);
    }

    @Override
    public String toString() {
        return "Passager{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
