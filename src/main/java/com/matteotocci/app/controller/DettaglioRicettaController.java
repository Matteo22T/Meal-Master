package com.matteotocci.app.controller;

import com.matteotocci.app.model.*;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DettaglioRicettaController {

    @FXML private Label nomeRicettaLabel;
    @FXML private TextArea descrizioneArea;
    @FXML private Label categoriaLabel;
    @FXML private Label autoreLabel;

    @FXML private Label kcalLabel;
    @FXML private Label proteineLabel;
    @FXML private Label carboidratiLabel;
    @FXML private Label grassiLabel;
    @FXML private Label grassiSaturiLabel;
    @FXML private Label zuccheriLabel;
    @FXML private Label fibreLabel;
    @FXML private Label saleLabel;

    @FXML private TableView<IngredienteVisuale> ingredientiTable;
    @FXML private TableColumn<IngredienteVisuale, String> nomeCol;
    @FXML private TableColumn<IngredienteVisuale, Double> quantitaCol;

    private Ricetta ricetta;

    public void setRicetta(Ricetta ricetta) {
        this.ricetta = ricetta;
        caricaDatiRicetta();
    }

    private void caricaDatiRicetta() {
        String queryRicetta = "SELECT r.nome, r.descrizione, r.id_utente,r.categoria, u.nome AS nome_autore, u.cognome AS cognome_autore, " +
                "r.kcal, r.proteine, r.carboidrati, r.zuccheri, r.grassi, r.grassi_saturi, r.fibre, r.sale " +
                "FROM Ricette r JOIN Utente u ON r.id_utente = u.id WHERE r.id = ?";

        String queryIngredienti = "SELECT f.nome, ir.quantita_grammi FROM ingredienti_ricette ir " +
                "JOIN foods f ON ir.id_alimento = f.id WHERE ir.id_ricetta = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmtRicetta = conn.prepareStatement(queryRicetta);
             PreparedStatement stmtIngredienti = conn.prepareStatement(queryIngredienti)) {

            stmtRicetta.setInt(1, ricetta.getId());
            ResultSet rs = stmtRicetta.executeQuery();

            if (rs.next()) {
                nomeRicettaLabel.setText(rs.getString("nome"));
                descrizioneArea.setText(rs.getString("descrizione"));
                categoriaLabel.setText(rs.getString("categoria"));

                kcalLabel.setText(String.format("%.1f", rs.getDouble("kcal")));
                proteineLabel.setText(String.format("%.1f", rs.getDouble("proteine")));
                carboidratiLabel.setText(String.format("%.1f", rs.getDouble("carboidrati")));
                zuccheriLabel.setText(String.format("%.1f", rs.getDouble("zuccheri")));
                grassiLabel.setText(String.format("%.1f", rs.getDouble("grassi")));
                grassiSaturiLabel.setText(String.format("%.1f", rs.getDouble("grassi_saturi")));
                fibreLabel.setText(String.format("%.1f", rs.getDouble("fibre")));
                saleLabel.setText(String.format("%.1f", rs.getDouble("sale")));

                int idUtente = rs.getInt("id_utente");
                caricaAutore(idUtente, conn); // passa la connessione anche al metodo autore
            }

            stmtIngredienti.setInt(1, ricetta.getId());
            ResultSet rs2 =stmtIngredienti.executeQuery();

            ObservableList<IngredienteVisuale> lista = FXCollections.observableArrayList();

            while(rs2.next()) {
                String nomeAlimento = rs2.getString("nome");
                int quantita = rs2.getInt("quantita_grammi");
                lista.add(new IngredienteVisuale(nomeAlimento, quantita));

            }
            nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
            quantitaCol.setCellValueFactory(new PropertyValueFactory<>("quantita"));
            ingredientiTable.setItems(lista);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void caricaAutore(int idUtente, Connection conn) {
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nomeCompleto = rs.getString("Nome") + " " + rs.getString("Cognome");
                autoreLabel.setText(nomeCompleto);
            } else {
                autoreLabel.setText("Sconosciuto");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class IngredienteVisuale {
        private final SimpleStringProperty nome;
        private final SimpleDoubleProperty quantita;

        public IngredienteVisuale(String nome, double quantita) {
            this.nome = new SimpleStringProperty(nome);
            this.quantita = new SimpleDoubleProperty(quantita);
        }

        public String getNome() {
            return nome.get();
        }

        public double getQuantita() {
            return quantita.get();
        }

    }










}
