package com.matteotocci.app.model;

import org.mindrot.jbcrypt.BCrypt; // Importa la libreria bcrypt
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteModel {

    Connection conn;

    public UtenteModel() {
        conn = SQLiteConnessione.connector();
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
                // Usa BCrypt.checkpw per confrontare la password inserita con l'hash del database
                return BCrypt.checkpw(vecchiaPasswordInserita, passwordDatabase);
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
            // **IMPORTANTE: Hash della nuova password PRIMA di salvarla**
            String hashedPassword = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}