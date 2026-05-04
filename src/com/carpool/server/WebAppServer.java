package com.carpool.server;

import com.carpool.db.DatabaseManager;
import com.carpool.model.entities.*;
import com.carpool.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class WebAppServer {
    private static TrajetService trajetService = new TrajetService();
    private static AuthService authService = new AuthService();
    private static NotificationService notifService = new NotificationService();
    private static PaiementService paiementService = new PaiementService(notifService);
    private static ReservationService reservationService = new ReservationService(paiementService, notifService, trajetService, authService);

    public static void main(String[] args) throws Exception {
        DatabaseManager.initDatabase();
        initMockData();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/trajets", new ApiTrajetsHandler());
        server.createContext("/api/trajets/cancel", new ApiCancelTrajetHandler());
        server.createContext("/api/reservations", new ApiReservationsHandler());
        server.createContext("/api/reservations/cancel", new ApiCancelReservationHandler());
        server.createContext("/api/auth/login", new ApiLoginHandler());
        server.createContext("/api/auth/register", new ApiRegisterHandler());
        server.createContext("/api/dashboard", new ApiDashboardHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("====== NexRide Web Server Started ======");
        System.out.println("Listen on: http://localhost:8080");
        System.out.println("Serving frontend from: /frontend/");
        System.out.println("Database Engine: H2 Embedded Database");
    }

    private static void initMockData() {
        try {
            if(authService.getUserById("C1") == null) {
                Chauffeur c = new Chauffeur("C1", "Dupont", "Marc", "marc@test.com", "pass123");
                Passager p = new Passager("P1", "Martin", "Sophie", "sophie@test.com", "pass456");
                p.addMoyenPaiement(new MoyenPaiement("MP1", "CARTE_BANCAIRE", "4532 **** **** 1234", "12/26"));
                authService.registerUser(c);
                authService.registerUser(p);
                trajetService.addTrajet(new Trajet("T1", "C1", "Paris", "Lyon", LocalDateTime.now().plusDays(2), 3, 35.0));
                trajetService.addTrajet(new Trajet("T2", "C1", "Marseille", "Nice", LocalDateTime.now().plusDays(5), 4, 20.0));
                System.out.println("Generated Mock Data into H2 DB.");
            }
        } catch(Exception e) {}
    }

    private static void sendResp(HttpExchange t, int code, String response) throws IOException {
        t.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static Map<String, String> parseForm(String formData) {
        Map<String, String> result = new java.util.HashMap<>();
        for (String param : formData.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                result.put(java.net.URLDecoder.decode(pair[0], java.nio.charset.StandardCharsets.UTF_8), 
                           java.net.URLDecoder.decode(pair[1], java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            File file = new File("frontend" + path);
            if (file.exists() && !file.isDirectory()) {
                t.sendResponseHeaders(200, file.length());
                OutputStream os = t.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            } else {
                sendResp(t, 404, "Frontend File Not Found");
            }
        }
    }

    static class ApiTrajetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("GET".equals(t.getRequestMethod())) {
                String query = t.getRequestURI().getQuery();
                if (query == null || !query.startsWith("userId=")) {
                    sendResp(t, 401, "{\"error\": \"Unauthorized. Log in first.\"}");
                    return;
                }
                t.getResponseHeaders().add("Content-Type", "application/json");
                String jsonArray = "[" + trajetService.getAllTrajets().stream()
                    .filter(tr -> tr.getStatus() != com.carpool.model.enums.TrajetStatus.ANNULE && tr.getPlacesDisponibles() > 0)
                    .map(tr -> {
                    User chauffeur = authService.getUserById(tr.getChauffeurId());
                    String nomChauffeur = chauffeur != null ? chauffeur.getPrenom() + " " + chauffeur.getNom().substring(0, 1) + "." : "Inconnu";
                    return String.format("{\"id\":\"%s\", \"depart\":\"%s\", \"arrivee\":\"%s\", \"date\":\"%s\", \"prix\":%.2f, \"places\":%d, \"chauffeurNom\":\"%s\"}", 
                    tr.getId(), tr.getPointDepart(), tr.getPointArrivee(), tr.getDateHeure(), tr.getPrixParPlace(), tr.getPlacesDisponibles(), nomChauffeur);
                }).collect(Collectors.joining(",")) + "]";
                sendResp(t, 200, jsonArray);
            } else if ("POST".equals(t.getRequestMethod())) {
                try {
                    String body = new String(t.getRequestBody().readAllBytes());
                    Map<String, String> p = parseForm(body);
                    String id = "T" + System.currentTimeMillis();
                    String chauffeurId = p.get("userId");
                    
                    Trajet tr = new Trajet(id, chauffeurId, p.get("depart"), p.get("arrivee"), LocalDateTime.parse(p.get("date")), Integer.parseInt(p.get("places")), Double.parseDouble(p.get("prix")));
                    trajetService.addTrajet(tr);
                    sendResp(t, 200, "{\"status\":\"success\"}");
                } catch(Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            } else {
                sendResp(t, 405, "{\"status\":\"error\"}");
            }
        }
    }

    static class ApiCancelTrajetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                try {
                    String body = new String(t.getRequestBody().readAllBytes());
                    Map<String, String> p = parseForm(body);
                    String trajetId = p.get("trajetId");
                    if (trajetId == null) throw new Exception("trajetId manquant");
                    if (trajetService.getTrajet(trajetId) == null) throw new Exception("Trajet introuvable");

                    reservationService.annulerParChauffeur(trajetId);

                    t.getResponseHeaders().add("Content-Type", "application/json");
                    sendResp(t, 200, "{\"status\":\"success\"}");
                } catch (Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            } else {
                sendResp(t, 405, "{\"status\":\"error\"}");
            }
        }
    }

    static class ApiCancelReservationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                try {
                    String body = new String(t.getRequestBody().readAllBytes());
                    Map<String, String> p = parseForm(body);
                    String reservationId = p.get("reservationId");
                    if (reservationId == null) throw new Exception("reservationId manquant");

                    reservationService.annulerParPassager(reservationId);

                    t.getResponseHeaders().add("Content-Type", "application/json");
                    sendResp(t, 200, "{\"status\":\"success\"}");
                } catch (Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            } else {
                sendResp(t, 405, "{\"status\":\"error\"}");
            }
        }
    }

    static class ApiReservationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                try {
                    String body = new String(t.getRequestBody().readAllBytes());
                    Map<String, String> p = parseForm(body);
                    String trajetId = p.get("trajetId");
                    String passagerId = p.get("userId");
                    if(trajetId == null || passagerId == null) throw new Exception("Paramètres manquants");

                    Trajet tr = trajetService.getTrajet(trajetId);
                    if(tr == null) throw new Exception("Trajet inexistant");

                    Reservation res = new Reservation("R" + System.currentTimeMillis(), trajetId, passagerId, 1, tr.getPrixParPlace());
                    reservationService.creerReservation(res);
                    sendResp(t, 200, "{\"status\":\"success\"}");
                } catch(Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            } else {
                sendResp(t, 405, "{\"status\":\"error\"}");
            }
        }
    }
    
    static class ApiLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                String body = new String(t.getRequestBody().readAllBytes());
                Map<String, String> p = parseForm(body);
                try {
                    User u = authService.login(p.get("email"), p.get("password"));
                    sendResp(t, 200, "{\"status\":\"success\", \"userId\":\"" + u.getId() + "\", \"role\":\"" + u.getRole() + "\"}");
                } catch(Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            }
        }
    }

    static class ApiRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                String body = new String(t.getRequestBody().readAllBytes());
                Map<String, String> p = parseForm(body);
                try {
                    User u;
                    String id = "U" + System.currentTimeMillis();
                    String role = p.get("role");
                    if("CHAUFFEUR".equals(role)) {
                        u = new Chauffeur(id, p.get("nom"), p.get("prenom"), p.get("email"), p.get("password"));
                    } else {
                        u = new Passager(id, p.get("nom"), p.get("prenom"), p.get("email"), p.get("password"));
                        ((Passager)u).addMoyenPaiement(new MoyenPaiement("MP"+id, "VIRTUAL_CREDIT", "0000 0000 0000 0000", "01/30"));
                    }
                    authService.registerUser(u);
                    sendResp(t, 200, "{\"status\":\"success\"}");
                } catch(Exception e) {
                    sendResp(t, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage().replace("\"", "'") + "\"}");
                }
            }
        }
    }

    static class ApiDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            if (query == null || !query.startsWith("userId=")) {
                sendResp(t, 400, "{\"error\": \"userId required\"}");
                return;
            }
            String userId = query.split("=")[1];
            User u = authService.getUserById(userId);
            if (u == null) {
                sendResp(t, 404, "{\"error\": \"user not found\"}");
                return;
            }

            t.getResponseHeaders().add("Content-Type", "application/json");
            
            List<String> items = new java.util.ArrayList<>();
            List<LocalDateTime> upcomings = new java.util.ArrayList<>();
            int totalEntity = 0;
            
            if (u instanceof Passager) {
                List<Reservation> resList = reservationService.getReservationsForUser(userId);
                totalEntity = resList.size();
                for (Reservation r : resList) {
                    Trajet tr = trajetService.getTrajet(r.getTrajetId());
                    if (tr != null) {
                        items.add(String.format("{\"depart\":\"%s\", \"arrivee\":\"%s\", \"date\":\"%s\", \"status\":\"%s\", \"id\":\"%s\"}",
                            tr.getPointDepart(), tr.getPointArrivee(), tr.getDateHeure(), r.getStatus(), r.getId()));
                        if(tr.getDateHeure().isAfter(LocalDateTime.now())) {
                            upcomings.add(tr.getDateHeure());
                        }
                    }
                }
            } else if (u instanceof Chauffeur) {
                List<Trajet> tList = trajetService.getTrajetsForChauffeur(userId);
                totalEntity = tList.size();
                for (Trajet tr : tList) {
                    items.add(String.format("{\"depart\":\"%s\", \"arrivee\":\"%s\", \"date\":\"%s\", \"status\":\"%s\", \"id\":\"%s\"}",
                        tr.getPointDepart(), tr.getPointArrivee(), tr.getDateHeure(), tr.getStatus(), tr.getId()));
                    if(tr.getDateHeure().isAfter(LocalDateTime.now())) {
                        upcomings.add(tr.getDateHeure());
                    }
                }
            }

            String recentTripsJson = "[" + String.join(",", items) + "]";
            
            String prochainDepart = "Aucun";
            if(!upcomings.isEmpty()) {
                upcomings.sort(LocalDateTime::compareTo);
                prochainDepart = upcomings.get(0).toString().split("T")[0];
            }

            List<String> notifList = new java.util.ArrayList<>();
            double avis = 0.0;
            try (java.sql.Connection conn = com.carpool.db.DatabaseManager.getConnection()) {
                java.sql.PreparedStatement ps = conn.prepareStatement("SELECT avis FROM USERS WHERE id=?");
                ps.setString(1, userId);
                java.sql.ResultSet rs = ps.executeQuery();
                if(rs.next()) avis = rs.getDouble("avis");
                
                java.sql.PreparedStatement psNotif = conn.prepareStatement("SELECT * FROM NOTIFICATIONS WHERE userId=?");
                psNotif.setString(1, userId);
                java.sql.ResultSet rsNotif = psNotif.executeQuery();
                while(rsNotif.next()) {
                    notifList.add(String.format("{\"id\":\"%s\", \"message\":\"%s\", \"time\":\"%s\"}", 
                        rsNotif.getString("id"), rsNotif.getString("message").replace("\"", "'"), rsNotif.getString("time").split("T")[0]));
                }
            } catch(Exception e) { e.printStackTrace(); }

            String notificationsJson = "[" + String.join(",", notifList) + "]";

            String json = String.format(
                "{\"stats\": {\"trajetsEffectues\": %d, \"avis\": %.1f, \"prochainDepart\": \"%s\"}, " +
                "\"recentTrips\": %s, \"notifications\": %s}",
                totalEntity, avis, prochainDepart, recentTripsJson, notificationsJson
            );

            sendResp(t, 200, json);
        }
    }
}
