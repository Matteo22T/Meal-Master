package com.matteotocci.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteModel {

    Connection conn;

    public UtenteModel() {
        conn = SQLiteConnessione.connector(); // Assicurati che la tua connessione sia gestita correttamente
        if (conn == null) {
            System.out.println("Connessione al database fallita.");
            System.exit(1);
        }
    }

    public boolean verificaVecchiaPassword(String userId, String vecchiaPasswordInserita) {
        String query = "SELECT password FROM Utente WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String passwordDatabase = rs.getString("password");
                // **IMPORTANTE: Qui dovresti confrontare la vecchiaPasswordInserita (HASHED) con passwordDatabase**
                // Per ora, per semplicità, faccio un confronto diretto (NON SICURO PER LA PRODUZIONE)
                return vecchiaPasswordInserita.equals(passwordDatabase);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean aggiornaPassword(String userId, String nuovaPassword) throws SQLException {
        String query = "UPDATE Utente SET password = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // **IMPORTANTE: Qui dovresti fare l'HASH della nuovaPassword prima di salvarla nel database**
            pstmt.setString(1, nuovaPassword);
            pstmt.setString(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Potresti aggiungere altri metodi qui per gestire i dati dell'utente (es. recuperare il nome, l'email, ecc.)
}