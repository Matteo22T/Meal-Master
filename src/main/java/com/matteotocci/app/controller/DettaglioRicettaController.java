package com.matteotocci.app.controller;

import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Ricetta;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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

    @FXML private TableView<IngredienteView> ingredientiTable;
    @FXML private TableColumn<IngredienteView, String> nomeCol;
    @FXML private TableColumn<IngredienteView, Double> quantitaCol;

    private Ricetta ricetta;

    public void setRicetta(Ricetta ricetta) {
        this.ricetta = ricetta;
        caricaDettagli();
    }

    private void caricaDettagli() {
        nomeRicettaLabel.setText(ricetta.getNome());
        descrizioneArea.setText(ricetta.getDescrizione());
        categoriaLabel.setText("Categoria: " + ricetta.getCategoria());

        String query = """
            SELECT f.nome AS alimento_nome, i.quantita_grammi,
                   u.Nome AS nome_utente, u.Cognome AS cognome_utente,
                   r.kcal, r.proteine, r.carboidrati, r.grassi, r.grassiSaturi, r.zuccheri, r.fibre, r.sale
            FROM Ricette r
            JOIN ingredienti_ricette i ON r.id = i.id_ricetta
            JOIN foods f ON f.id = i.id_alimento
            JOIN Utente u ON r.user_id = u.id
            WHERE r.id = ?
        """;

        ObservableList<IngredienteView> ingredienti = FXCollections.observableArrayList();

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, ricetta.getId());
            ResultSet rs = stmt.executeQuery();

            boolean first = true;
            while (rs.next()) {
                ingredienti.add(new IngredienteView(
                        rs.getString("alimento_nome"),
                        rs.getDouble("quantita_grammi")
                ));

                if (first) {
                    autoreLabel.setText("Autore: " + rs.getString("nome_utente") + " " + rs.getString("cognome_utente"));

                    kcalLabel.setText(String.format("%.1f kcal", rs.getDouble("kcal")));
                    proteineLabel.setText(String.format("%.1f g", rs.getDouble("proteine")));
                    carboidratiLabel.setText(String.format("%.1f g", rs.getDouble("carboidrati")));
                    grassiLabel.setText(String.format("%.1f g", rs.getDouble("grassi")));
                    grassiSaturiLabel.setText(String.format("%.1f g", rs.getDouble("grassiSaturi")));
                    zuccheriLabel.setText(String.format("%.1f g", rs.getDouble("zuccheri")));
                    fibreLabel.setText(String.format("%.1f g", rs.getDouble("fibre")));
                    saleLabel.setText(String.format("%.1f g", rs.getDouble("sale")));
                    first = false;
                }
            }

            nomeCol.setCellValueFactory(data -> data.getValue().nomeProperty());
            quantitaCol.setCellValueFactory(data -> data.getValue().quantitaProperty().asObject());

            ingredientiTable.setItems(ingredienti);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class IngredienteView {
        private final javafx.beans.property.SimpleStringProperty nome;
        private final javafx.beans.property.SimpleDoubleProperty quantita;

        public IngredienteView(String nome, double quantita) {
            this.nome = new javafx.beans.property.SimpleStringProperty(nome);
            this.quantita = new javafx.beans.property.SimpleDoubleProperty(quantita);
        }

        public javafx.beans.property.SimpleStringProperty nomeProperty() {
            return nome;
        }

        public javafx.beans.property.SimpleDoubleProperty quantitaProperty() {
            return quantita;
        }
    }
}
