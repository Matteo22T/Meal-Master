package com.matteotocci.app.model;

import org.mindrot.jbcrypt.BCrypt;
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
            // Chiama il metodo per rehash delle password all'avvio del modello
            rehashPasswords(); //aggiunto
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

    public int getIdUtente(String email) {
        String query = "SELECT id FROM Utente WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // errore
    }

    public String getRuoloUtente(String email) {
        String query = "SELECT ruolo FROM Utente WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ruolo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEmail(Integer id){
        String query = "SELECT Email FROM Utente WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM Utente WHERE Email = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Se il conteggio è maggiore di 0, l'email esiste
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la verifica dell'email: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // In caso di errore o se non trovato
    }

    public boolean registraUtente(String nome, String cognome, String email, String password, String ruolo) {
        if (emailExists(email)) {
            return false;
        }
        String query = "INSERT INTO Utente (nome, cognome, email, password, ruolo) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, email);
            // Hash della password prima di salvarla
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            ps.setString(4, hashedPassword);
            ps.setString(5, ruolo);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utente registrato con successo nel database (password hashata).");
                return true;
            } else {
                System.out.println("Nessuna riga inserita durante la registrazione.");
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

    public boolean verificaCredenziali(String email, String passwordInserita) {
        String query = "SELECT password FROM Utente WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String passwordHashDalDatabase = rs.getString("password");
                // Verifica se la password inserita corrisponde all'hash nel database
                return BCrypt.checkpw(passwordInserita, passwordHashDalDatabase);
            }
            return false; // Nessun utente trovato con questa email
        } catch (SQLException e) {
            System.err.println("Errore durante la verifica delle credenziali: " + e.getMessage());
            return false;
        }
    }
    private void rehashPasswords() {
        PreparedStatement selectStatement = null;
        PreparedStatement updateStatement = null;
        ResultSet resultSet = null;
        try {
            // Disabilita il journaling e la sincronizzazione per velocizzare gli aggiornamenti (solo per ambienti di sviluppo/manutenzione)
            try (Statement statement = conn.createStatement()) {
                statement.execute("PRAGMA journal_mode = OFF");
                statement.execute("PRAGMA synchronous = OFF");
            }
            // Seleziona tutte le righe dalla tabella Utente
            String selectQuery = "SELECT id, password FROM Utente";
            selectStatement = conn.prepareStatement(selectQuery);
            resultSet = selectStatement.executeQuery();

            // Prepara la query per aggiornare la password hashata
            String updateQuery = "UPDATE Utente SET password = ? WHERE id = ?";
            updateStatement = conn.prepareStatement(updateQuery);

            int updatedCount = 0;
            // Itera su ogni riga e rehasha la password
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String oldHashedPassword = resultSet.getString("password");

                // Verifica se la password è già hashata con bcrypt (per evitare di hashare hash)
                if (!oldHashedPassword.startsWith("$2a$")) { // $2a$ è il prefisso di jBCrypt
                    String newHashedPassword = BCrypt.hashpw(oldHashedPassword, BCrypt.gensalt());

                    updateStatement.setString(1, newHashedPassword);
                    updateStatement.setInt(2, id);
                    updateStatement.executeUpdate();
                    updatedCount++;
                    System.out.println("Rehashed password for user ID: " + id);
                }
                else{
                    System.out.println("Password for user ID: " + id + " is already hashed with bcrypt. Skipping.");
                }
            }
            System.out.println("Password rehash completed.  Total updated: " + updatedCount);

        } catch (SQLException e) {
            System.err.println("Errore durante il rehashing delle password: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Chiudi le risorse (ResultSet, PreparedStatement, Connection)
            try { if (resultSet != null) resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (selectStatement != null) selectStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (updateStatement != null) updateStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}