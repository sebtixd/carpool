package com.carpool.service;

import com.carpool.model.entities.Passager;
import com.carpool.model.entities.Chauffeur;
import com.carpool.model.entities.Reservation;
import com.carpool.exception.PaymentException;

public class PaiementService {
    private NotificationService notificationService;

    public PaiementService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void authorizePayment(Passager passager, Reservation reservation) {
        if (passager.getMoyensPaiement().isEmpty()) {
            throw new PaymentException("Aucun moyen de paiement disponible pour autoriser.");
        }
        reservation.setPaymentAuthorized(true);
        notificationService.sendNotification(passager, "Paiement de " + reservation.getTotalPrix() + " autorisé pour la réservation " + reservation.getId(), "PAYMENT_AUTH");
    }

    public void capturePayment(Reservation reservation) {
        if (!reservation.isPaymentAuthorized()) {
            throw new PaymentException("Paiement non autorisé.");
        }
        reservation.setPaymentCaptured(true);
    }

    public void processRefund(Passager passager, double amount) {
        notificationService.sendNotification(passager, "Remboursement de " + amount + " effectué.", "REFUND");
    }

    public void applyPenalty(Chauffeur chauffeur, double amount) {
        notificationService.sendNotification(chauffeur, "Pénalité de " + amount + " appliquée pour annulation tardive.", "PENALTY");
    }
}
