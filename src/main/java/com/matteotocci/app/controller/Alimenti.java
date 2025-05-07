package com.matteotocci.app.controller;

import com.matteotocci.app.model.Alimento;
import com.matteotocci.app.model.SQLiteConnessione;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;


import java.io.IOException;

public class Alimenti {
    @FXML
    private Button BottoneAlimenti;
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
    private Button BottoneHome;
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

    private String loggedInUserId; // Variabile per memorizzare l'ID dell'utente loggato

    @FXML
    private Label nomeUtenteLabelHomePage;

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        System.out.println("[DEBUG - HomePage] ID utente ricevuto: " + this.loggedInUserId);
        setNomeUtenteLabel();
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
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
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

    private void apriDettaglio(Alimento alimento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DettagliAlimento.fxml"));
            Parent root = loader.load();

            DettagliAlimentoController controller = loader.getController();
            controller.setAlimento(alimento);

            Stage stage = new Stage();
            stage.setTitle("Dettaglio Alimento");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML private TextField cercaAlimento;
    @FXML private TableView<Alimento> tableView;
    @FXML private Button bottoneCerca;


    @FXML private TableColumn<Alimento, ImageView> immagineCol;
    @FXML private TableColumn<Alimento, String> nomeCol, brandCol;
    @FXML private TableColumn<Alimento, Double> calorieCol, proteineCol, carboidratiCol, grassiCol;
    @FXML private TableColumn<Alimento, Double> grassiSatCol, saleCol, fibreCol, zuccheriCol;

    private int offset = 0;
    private final int LIMIT = 50;


    @FXML
    private void handleCercaAlimento(ActionEvent event) {
        offset = 0;
        tableView.getItems().clear();
        String filtro = cercaAlimento.getText();
        cercaAlimenti(filtro, false);
    }


    private ScrollBar getVerticalScrollbar(TableView<?> table) {
        for (Node node : table.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar) {
                ScrollBar sb = (ScrollBar) node;
                if (sb.getOrientation() == Orientation.VERTICAL) {
                    return sb;
                }
            }
        }
        return null;
    }

    private boolean isLoading = false;

    private void caricaAltri() {
        if (isLoading) return;
        isLoading = true;
        String filtro = cercaAlimento.getText();
        cercaAlimenti(filtro, true);
        isLoading = false;
    }



    @FXML
    public void initialize() {
        immagineCol.setCellValueFactory(new PropertyValueFactory<>("immagine"));
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        calorieCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        proteineCol.setCellValueFactory(new PropertyValueFactory<>("proteine"));
        carboidratiCol.setCellValueFactory(new PropertyValueFactory<>("carboidrati"));
        grassiCol.setCellValueFactory(new PropertyValueFactory<>("grassi"));
        grassiSatCol.setCellValueFactory(new PropertyValueFactory<>("grassiSaturi"));
        saleCol.setCellValueFactory(new PropertyValueFactory<>("sale"));
        fibreCol.setCellValueFactory(new PropertyValueFactory<>("fibre"));
        zuccheriCol.setCellValueFactory(new PropertyValueFactory<>("zuccheri"));

        tableView.setRowFactory(tv -> {
            TableRow<Alimento> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Alimento alimento = row.getItem();
                    apriDettaglio(alimento);
                }
            });
            return row;
        });



        Platform.runLater(() -> {
            ScrollBar scrollBar = getVerticalScrollbar(tableView);
            if (scrollBar != null) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() == scrollBar.getMax()) {
                        // Se raggiunge il fondo, carica altri 50
                        caricaAltri();
                    }
                });
            }
        });


    }




    private void cercaAlimenti(String filtro, boolean append) {
        ObservableList<Alimento> alimenti = append ? tableView.getItems(): FXCollections.observableArrayList();

        String query = "SELECT * FROM foods WHERE LOWER(nome) LIKE ? LIMIT ? OFFSET ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Imposta il parametro del filtro nella query
            stmt.setString(1, "%" + filtro.toLowerCase() + "%");
            stmt.setInt(2, LIMIT);
            stmt.setInt(3, offset);

            // Esegui la query
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alimenti.add(new Alimento(
                            rs.getString("nome"),
                            rs.getString("brand"),
                            rs.getDouble("kcal"),
                            rs.getDouble("proteine"),
                            rs.getDouble("carboidrati"),
                            rs.getDouble("grassi"),
                            rs.getDouble("grassiSaturi"),
                            rs.getDouble("sale"),
                            rs.getDouble("fibre"),
                            rs.getDouble("zuccheri"),
                            rs.getString("immaginePiccola"),
                            rs.getString("immagineGrande")
                    ));
                }

                // Imposta gli alimenti nella TableView
                tableView.setItems(alimenti);
                offset += LIMIT;


            } catch (SQLException e) {
                e.printStackTrace();  // Gestisci eventuali errori di ResultSet
            }

        } catch (SQLException e) {
            e.printStackTrace();  // Gestisci eventuali errori di connessione o query
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


}
