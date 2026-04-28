package com.carpool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:h2:./carpool_h2_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Updated to include security columns
            stmt.execute("CREATE TABLE IF NOT EXISTS USERS (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "nom VARCHAR(100), " +
                    "prenom VARCHAR(100), " +
                    "email VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(100), " +
                    "role VARCHAR(20), " +
                    "status VARCHAR(20) DEFAULT 'ACTIF', " +
                    "failedLoginAttempts INT DEFAULT 0, " +
                    "avis DOUBLE DEFAULT 0.0" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS NOTIFICATIONS (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "userId VARCHAR(50), " +
                    "message VARCHAR(255), " +
                    "time VARCHAR(100)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS TRAJETS (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "chauffeurId VARCHAR(50), " +
                    "pointDepart VARCHAR(100), " +
                    "pointArrivee VARCHAR(100), " +
                    "dateHeure VARCHAR(100), " +
                    "placesDisponibles INT, " +
                    "prixParPlace DOUBLE, " +
                    "status VARCHAR(20)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS RESERVATIONS (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "trajetId VARCHAR(50), " +
                    "passagerId VARCHAR(50), " +
                    "nbPlaces INT, " +
                    "totalPrix DOUBLE, " +
                    "status VARCHAR(20)" +
                    ");");
            System.out.println("Base de données H2 intialisée.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
