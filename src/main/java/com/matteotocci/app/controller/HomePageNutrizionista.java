package com.matteotocci.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.HBox; // Importa HBox

import java.io.IOException;
import java.sql.*;

public class HomePageNutrizionista {

    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Label nomeUtenteLabelHomePage;
    @FXML
    private TextField ricercaClienteTextField;
    @FXML
    private TableView<Cliente> tabellaClienti;
    @FXML
    private TableColumn<Cliente, String> nomeColonna;
    @FXML
    private TableColumn<Cliente, String> azioniColonna;

    private String loggedInUserId;
    private ObservableList<Cliente> listaClienti = FXCollections.observableArrayList();

    public void setLoggedInUserId(String userId) {
        this.loggedInUserId = userId;
        System.out.println("[DEBUG - HomePageNutrizionista] ID utente ricevuto: " + this.loggedInUserId);
        setNomeUtenteLabel();
        caricaClientiDelNutrizionista();
    }

    private void setNomeUtenteLabel() {
        String nomeUtente = getNomeUtenteDalDatabase(loggedInUserId);
        nomeUtenteLabelHomePage.setText(
                (nomeUtente != null && !nomeUtente.isEmpty()) ? nomeUtente : "Nome e Cognome"
        );
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nomeUtente = rs.getString("Nome") + " " + rs.getString("Cognome");
            }
        } catch (SQLException e) {
            System.err.println("Errore DB (nome utente): " + e.getMessage());
        }
        return nomeUtente;
    }

    @FXML
    private void initialize() {
        nomeColonna.setCellValueFactory(new PropertyValueFactory<>("nome"));

        azioniColonna.setCellFactory(new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) {
                return new TableCell<Cliente, String>() {
                    final Button visualizzaButton = new Button("Visualizza Dieta");
                    final Button modificaButton = new Button("Modifica Dieta");
                    final HBox container = new HBox(visualizzaButton, modificaButton); //Usa un HBox per disporre i bottoni orizzontalmente
                    {
                        container.setSpacing(5); // Spazio tra i bottoni
                        visualizzaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("Visualizza Dieta per: " + cliente.getNome());
                            // Aggiungi qui la logica per visualizzare la dieta (aprire una nuova schermata, ecc.)
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                                Parent visualizzaDietaRoot = fxmlLoader.load();
                                Stage visualizzaDietaStage = new Stage();
                                visualizzaDietaStage.setScene(new Scene(visualizzaDietaRoot));
                                visualizzaDietaStage.setTitle("Dieta di " + cliente.getNome());
                                visualizzaDietaStage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        });
                        modificaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("Modifica Dieta per: " + cliente.getNome());
                            // Aggiungi qui la logica per modificare la dieta (aprire una nuova schermata, ecc.)
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaDieta.fxml"));
                                Parent modificaDietaRoot = fxmlLoader.load();
                                Stage modificaDietaStage = new Stage();
                                modificaDietaStage.setScene(new Scene(modificaDietaRoot));
                                modificaDietaStage.setTitle("Modifica Dieta di " + cliente.getNome());
                                modificaDietaStage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container); //Imposta l'HBox come contenuto della cella
                        }
                    }
                };
            }
        });

        ricercaClienteTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            filtraClienti(newVal);
        });
    }

    private void caricaClientiDelNutrizionista() {
        listaClienti.clear();
        String url = "jdbc:sqlite:database.db";
        String query = "SELECT u.Nome, u.Cognome FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                listaClienti.add(new Cliente(nome));
            }
            tabellaClienti.setItems(listaClienti);
        } catch (SQLException e) {
            System.err.println("Errore DB (clienti): " + e.getMessage());
        }
    }

    private void filtraClienti(String filtro) {
        ObservableList<Cliente> filtrati = FXCollections.observableArrayList();
        if (filtro == null || filtro.isEmpty()) {
            filtrati.addAll(listaClienti);
        } else {
            String lower = filtro.toLowerCase();
            for (Cliente c : listaClienti) {
                if (c.getNome().toLowerCase().contains(lower)) {
                    filtrati.add(c);
                }
            }
        }
        tabellaClienti.setItems(filtrati);
    }

    public static class Cliente {
        private String nome;

        public Cliente(String nome) {
            this.nome = nome;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

    }
}

