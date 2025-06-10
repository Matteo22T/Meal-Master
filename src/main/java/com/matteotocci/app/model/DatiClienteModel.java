package com.matteotocci.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class DatiClienteModel {
    Connection conn;

    public DatiClienteModel() {
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



    public boolean registraCliente(double altezza, double peso, LocalDate dataDiNascita, String livelloAttivita, String sesso, int idNutrizionista, int idUtente){
        String query = "INSERT INTO Clienti (id_cliente, altezza_cm, peso_kg, livello_attivita, data_di_nascita, id_nutrizionista, sesso  ) VALUES (?, ?, ?, ?, ?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setDouble(2, altezza);
            ps.setDouble(3, peso);
            ps.setString(5, dataDiNascita.toString());
            ps.setString(4, livelloAttivita);
            ps.setString(7, sesso);
            ps.setInt(6, idNutrizionista);
            ps.setInt(1, idUtente);
            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected);
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
