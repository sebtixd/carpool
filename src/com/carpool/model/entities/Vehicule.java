package com.carpool.model.entities;

import java.util.Objects;

public class Vehicule {
    private String id;
    private String chauffeurId;
    private String marque;
    private String modele;
    private String immatriculation;

    public Vehicule(String id, String chauffeurId, String marque, String modele, String immatriculation) {
        this.id = id;
        this.chauffeurId = chauffeurId;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
    }

    public String getId() { return id; }
    public String getChauffeurId() { return chauffeurId; }
    public String getMarque() { return marque; }
    public String getModele() { return modele; }
    public String getImmatriculation() { return immatriculation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(id, vehicule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "id='" + id + '\'' +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                '}';
    }
}
