package com.matteotocci.app.model;
import java.sql.*;
public class LoginModel {
    Connection conn;

    public LoginModel() {
        conn = SQLiteConnessione.connector();
        if (conn == null) {
            System.out.println("Connessione non riuscita");
            System.exit(1);
        }
        try {
            System.out.println("Database in uso: " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isDbConnected() {
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registraUtente(String nome, String cognome, String email, String password) {
        String query = "INSERT INTO Utente (nome, cognome, email, password) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = null; // Inizializza a null prima del try
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, email);
            ps.setString(4, password);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utente registrato con successo nel database."); // Aggiungi un log di successo
                return true;
            } else {
                System.out.println("Nessuna riga inserita nel database."); // Aggiungi un log di insuccesso
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la registrazione dell'utente: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    System.err.println("Errore durante la chiusura del PreparedStatement: " + e.getMessage());
                }
            }
        }
    }
}