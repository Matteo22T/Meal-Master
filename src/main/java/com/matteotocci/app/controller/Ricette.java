package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.Ricetta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;


import java.io.IOException;
import java.sql.*;

public class Ricette {
    @FXML private Button BottoneAlimenti;
    @FXML private Button BottoneHome;
    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato
    @FXML private Label nomeUtenteLabelHomePage;
    @FXML private Button BottoneRicette;
    @FXML private ComboBox<String> categorieRicette;
    @FXML private CheckBox mieiAlimentiCheckBox;


    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneRicette.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneAlimenti.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoHome(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();
            ((Stage) BottoneHome.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();

            // Ottieni il controller PaginaProfilo
            PaginaProfilo profileController = fxmlLoader.getController();

            // **Usa l'ID utente memorizzato invece della stringa statica**
            if (loggedInUserId != null) {
                System.out.println("[DEBUG - HomePage] ID utente da passare a Profilo: " + loggedInUserId);
                profileController.setUtenteCorrenteId(loggedInUserId);
            } else {
                System.out.println("[DEBUG - HomePage] ID utente non ancora disponibile per il Profilo.");
                // Potresti voler gestire questo caso mostrando un messaggio o disabilitando l'accesso al profilo
            }

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(loggedInUserId);
        if (nomeUtente != null && !nomeUtente.isEmpty()) {
            nomeUtenteLabelHomePage.setText(nomeUtente);
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome"); // Fallback text
        }
    }
    private String getNomeUtenteDalDatabase(String userId) {
        String nome = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT Nome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nome = rs.getString("Nome");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura del nome utente dal database: " + e.getMessage());
        }
        return nome;
    }



    @FXML private TextField cercaRicetta;
    @FXML private TableView<Ricetta> tableViewRicette;
    @FXML private TableColumn<Ricetta, String> nomeCol;
    @FXML private TableColumn<Ricetta, String> descrizioneCol;
    @FXML private TableColumn<Ricetta, String> categoriaCol;


    private int offset = 0;
    private final int LIMIT = 50;
    private boolean isLoading = false;

    @FXML
    public void initialize() {
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        descrizioneCol.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        cercaRicette("", false);

        popolaCategorie();

        categorieRicette.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        mieiAlimentiCheckBox.setOnAction(e -> {
            offset = 0;
            tableViewRicette.getItems().clear();
            cercaRicette(cercaRicetta.getText(), false);
        });

        ScrollBar scrollBar = getVerticalScrollbar(tableViewRicette);
        if (scrollBar != null) {
            scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() == scrollBar.getMax()) {
                    caricaAltri();
                }
            });
        }
    }

    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        for (javafx.scene.Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                return sb;
            }
        }
        return null;
    }

    private void caricaAltri() {
        if (!isLoading) {
            isLoading = true;
            cercaRicette(cercaRicetta.getText(), true);
            isLoading = false;
        }
    }


    @FXML
    private void handleCercaRicetta(ActionEvent event) {
        offset = 0;
        tableViewRicette.getItems().clear();
        cercaRicette(cercaRicetta.getText(), false);
    }


    private void cercaRicette(String filtro, boolean append) {
        ObservableList<Ricetta> ricette = append ? tableViewRicette.getItems() : FXCollections.observableArrayList();

        String categoria = categorieRicette.getSelectionModel().getSelectedItem();
        boolean soloMiei = mieiAlimentiCheckBox.isSelected();
        StringBuilder query = new StringBuilder("SELECT * FROM Ricette WHERE LOWER(nome) LIKE ?");
        if (categoria != null && !categoria.equals("Tutte")) {
            query.append(" AND categoria = ?");
        }
        if (soloMiei && Session.getUserId() != null) {
            query.append(" AND id_utente = ?");
        }
        query.append(" LIMIT ? OFFSET ?");
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + filtro.toLowerCase() + "%");
            if (categoria != null && !categoria.equals("Tutte")) {
                stmt.setString(paramIndex++, categoria);
            }
            if (soloMiei && Session.getUserId() != null) {
                stmt.setInt(paramIndex++, Session.getUserId());
            }
            stmt.setInt(paramIndex++, LIMIT);
            stmt.setInt(paramIndex++, offset);

            try(ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Ricetta ricetta = new Ricetta(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descrizione"),
                            rs.getString("categoria"),
                            rs.getInt("id_utente")
                    );
                    ricette.add(ricetta);
                }

                tableViewRicette.setItems(ricette);
                offset += LIMIT;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void popolaCategorie(){
        ObservableList<String> categoriePrefissate = FXCollections.observableArrayList(
                "Tutte","Colazione", "Spuntino", "Pranzo", "Merenda", "Cena"
        );

        categorieRicette.setItems(categoriePrefissate);
        categorieRicette.getSelectionModel().selectFirst();

    }



    @FXML
    private void apriDettaglioRicetta(MouseEvent event) {
        if (event.getClickCount() == 2 && !tableViewRicette.getSelectionModel().isEmpty()) {
            Ricetta ricettaSelezionata = tableViewRicette.getSelectionModel().getSelectedItem();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettaglioRicetta.fxml"));
                Parent root = loader.load();

                DettaglioRicettaController controller = loader.getController();
                controller.setRicetta(ricettaSelezionata);

                Stage stage = new Stage();
                stage.setTitle("Dettaglio Ricetta");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    @FXML
    private void handleApriAggiuntaRicetta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiRicetta.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Aggiungi Ricetta");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
