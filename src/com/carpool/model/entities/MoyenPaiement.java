package com.carpool.model.entities;

import java.util.Objects;

public class MoyenPaiement {
    private String id;
    private String passagerId;
    private String type; // e.g. "CB", "PAYPAL"
    private String details;

    public MoyenPaiement(String id, String passagerId, String type, String details) {
        this.id = id;
        this.passagerId = passagerId;
        this.type = type;
        this.details = details;
    }

    public String getId() { return id; }
    public String getPassagerId() { return passagerId; }
    public String getType() { return type; }
    public String getDetails() { return details; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoyenPaiement that = (MoyenPaiement) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MoyenPaiement{" +
                "id='" + id + '\'' +
                ", passagerId='" + passagerId + '\'' +
                ", type='" + type + '\'' +
                ", details='***'" +
                '}';
    }
}
