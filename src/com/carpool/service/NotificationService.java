package com.carpool.service;

import com.carpool.model.entities.User;

import com.carpool.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class NotificationService {
    public void sendNotification(User user, String message, String type) {
        user.receiveNotification(message, type);
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO NOTIFICATIONS VALUES (?, ?, ?, ?)");
            ps.setString(1, "N" + System.currentTimeMillis());
            ps.setString(2, user.getId());
            ps.setString(3, "[" + type + "] " + message);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
