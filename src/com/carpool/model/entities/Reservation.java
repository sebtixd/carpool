package com.carpool.model.entities;

import com.carpool.model.enums.ReservationStatus;

import java.util.Objects;

public class Reservation {
    private String id;
    private String trajetId;
    private String passagerId;
    private int nbPlaces;
    private double totalPrix;
    private ReservationStatus status;
    private boolean paymentAuthorized;
    private boolean paymentCaptured;

    public Reservation(String id, String trajetId, String passagerId, int nbPlaces, double prixParPlace) {
        this.id = id;
        this.trajetId = trajetId;
        this.passagerId = passagerId;
        this.nbPlaces = nbPlaces;
        this.totalPrix = nbPlaces * prixParPlace;
        this.status = ReservationStatus.EN_ATTENTE;
        this.paymentAuthorized = false;
        this.paymentCaptured = false;
    }

    public Reservation(String id, String trajetId, String passagerId, int nbPlaces, double totalPrix, ReservationStatus status) {
        this.id = id;
        this.trajetId = trajetId;
        this.passagerId = passagerId;
        this.nbPlaces = nbPlaces;
        this.totalPrix = totalPrix;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTrajetId() { return trajetId; }
    public String getPassagerId() { return passagerId; }
    public int getNbPlaces() { return nbPlaces; }
    public double getTotalPrix() { return totalPrix; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    
    public boolean isPaymentAuthorized() { return paymentAuthorized; }
    public void setPaymentAuthorized(boolean paymentAuthorized) { this.paymentAuthorized = paymentAuthorized; }
    
    public boolean isPaymentCaptured() { return paymentCaptured; }
    public void setPaymentCaptured(boolean paymentCaptured) { this.paymentCaptured = paymentCaptured; }
}
