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
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.*;

import com.matteotocci.app.model.Dieta; // Importa il modello Dieta
import com.matteotocci.app.model.SQLiteConnessione; // Importa la tua classe di connessione al DB

public class HomePageNutrizionista {

    @FXML
    private Button BottoneDieta;
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

        // Configurazione della cella per la colonna Azioni
        azioniColonna.setCellFactory(new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) {
                return new TableCell<Cliente, String>() {
                    final Button visualizzaButton = new Button("Visualizza Dieta");
                    final Button modificaButton = new Button("Modifica Dieta");
                    final HBox container = new HBox(visualizzaButton, modificaButton);
                    {
                        container.setSpacing(5); // Spazio tra i bottoni

                        // AZIONE DEL PULSANTE "VISUALIZZA DIETA"
                        visualizzaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("DEBUG (HomePageNutrizionista): Click su Visualizza Dieta per cliente: " + cliente.getNome() + " (ID: " + cliente.getId() + ")");

                            // PASSO 1: Recupera la Dieta assegnata a questo cliente dal database
                            Dieta dietaAssegnata = recuperaDietaAssegnataACliente(cliente.getId());

                            if (dietaAssegnata != null) {
                                try {
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                                    Parent visualizzaDietaRoot = fxmlLoader.load();

                                    // PASSO 2: Ottieni il controller della nuova finestra
                                    VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                                    // PASSO 3: Passa l'oggetto Dieta al controller della nuova finestra
                                    visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                                    System.out.println("DEBUG (HomePageNutrizionista): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                                    Stage visualizzaDietaStage = new Stage();
                                    visualizzaDietaStage.setScene(new Scene(visualizzaDietaRoot));
                                    visualizzaDietaStage.setTitle("Dieta di " + cliente.getNome());
                                    visualizzaDietaStage.show();

                                } catch (IOException e) {
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.", "Verificare il percorso del file FXML.");
                                } catch (Exception e) {
                                    System.err.println("ERRORE (HomePageNutrizionista): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                                    e.printStackTrace();
                                    showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.", "Dettagli: " + e.getMessage());
                                }
                            } else {
                                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente " + cliente.getNome() + " (ID: " + cliente.getId() + ").");
                                showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata",
                                        "Il cliente selezionato non ha diete assegnate o non è stato possibile recuperarle.");
                            }
                        });

                        // AZIONE DEL PULSANTE "MODIFICA DIETA" (codice originale, non modificato in questo contesto)
                        modificaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("DEBUG (HomePageNutrizionista): Modifica Dieta per: " + cliente.getNome());
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
                            setGraphic(container);
                        }
                    }
                };
            }
        });

        ricercaClienteTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            filtraClienti(newVal);
        });
    }

    // Metodo per recuperare la dieta assegnata a un cliente specifico
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db";
        // Query per recuperare la dieta con l'ID del cliente
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector(); // Usa SQLiteConnessione.connector()
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"), // Assicurati che il costruttore di Dieta supporti questi campi
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    private void caricaClientiDelNutrizionista() {
        listaClienti.clear();
        String url = "jdbc:sqlite:database.db";
        // Modifica la query per recuperare anche l'ID dell'utente che è il cliente
        String query = "SELECT u.id, u.Nome, u.Cognome FROM Utente u " + // AGGIUNTO: u.id
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ?";
        try (Connection conn = SQLiteConnessione.connector(); // Usa SQLiteConnessione.connector()
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, loggedInUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int clienteId = rs.getInt("id"); // Recupera l'ID del cliente
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                listaClienti.add(new Cliente(clienteId, nome)); // Passa l'ID al costruttore
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

    // Classe interna Cliente - MODIFICATA per includere l'ID
    public static class Cliente {
        private String nome;
        private int id; // AGGIUNTO: ID del cliente

        public Cliente(int id, String nome) { // AGGIUNTO: Costruttore con ID
            this.id = id;
            this.nome = nome;
        }

        public int getId() { // AGGIUNTO: Getter per l'ID
            return id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    // Metodo per navigazione a DietaNutrizionista (precedentemente fornito)
    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();
            DietaNutrizionista controller = fxmlLoader.getController();
            controller.setLoggedInUserId(loggedInUserId); // Passa l'ID Utente
            Stage dietaStage = new Stage();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete");
            dietaStage.show();
            ((Stage) BottoneDieta.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per navigazione a ProfiloNutrizionista (precedentemente fornito)
    @FXML
    private void openProfiloNutrizionista(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml"));
            Parent profileRoot = fxmlLoader.load();

            ProfiloNutrizionista profileController = fxmlLoader.getController();
            profileController.setLoggedInUserId(loggedInUserId); // Passa l'ID utente

            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo helper per mostrare alert
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

