package com.carpool.model.entities;

import com.carpool.exception.InsufficientSeatsException;
import com.carpool.model.enums.TrajetStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class Trajet {
    private String id;
    private String chauffeurId;
    private String pointDepart;
    private String pointArrivee;
    private LocalDateTime dateHeure;
    private int placesDisponibles;
    private double prixParPlace;
    private TrajetStatus status;

    public Trajet(String id, String chauffeurId, String pointDepart, String pointArrivee, LocalDateTime dateHeure, int placesDisponibles, double prixParPlace) {
        this.id = id;
        this.chauffeurId = chauffeurId;
        this.pointDepart = pointDepart;
        this.pointArrivee = pointArrivee;
        this.dateHeure = dateHeure;
        this.placesDisponibles = placesDisponibles;
        this.prixParPlace = prixParPlace;
        this.status = TrajetStatus.PLANIFIE;
    }

    public void reserverPlaces(int nbPlaces) {
        if (this.placesDisponibles < nbPlaces) {
            throw new InsufficientSeatsException("Pas assez de places disponibles");
        }
        this.placesDisponibles -= nbPlaces;
        if (this.placesDisponibles == 0) {
            this.status = TrajetStatus.COMPLET;
        }
    }

    public void annulerReservation(int nbPlaces) {
        this.placesDisponibles += nbPlaces;
        if (this.placesDisponibles > 0 && this.status == TrajetStatus.COMPLET) {
            this.status = TrajetStatus.PLANIFIE;
        }
    }

    public String getId() { return id; }
    public String getChauffeurId() { return chauffeurId; }
    public String getPointDepart() { return pointDepart; }
    public String getPointArrivee() { return pointArrivee; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public int getPlacesDisponibles() { return placesDisponibles; }
    public double getPrixParPlace() { return prixParPlace; }
    public TrajetStatus getStatus() { return status; }
    
    public void setStatus(TrajetStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trajet trajet = (Trajet) o;
        return Objects.equals(id, trajet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Trajet{" +
                "id='" + id + '\'' +
                ", pointDepart='" + pointDepart + '\'' +
                ", pointArrivee='" + pointArrivee + '\'' +
                ", dateHeure=" + dateHeure +
                ", places=" + placesDisponibles +
                ", prix=" + prixParPlace +
                ", status=" + status +
                '}';
    }
}
