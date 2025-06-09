package com.matteotocci.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager; // Mantenuto se usato da getNomeUtenteDalDatabase
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.SQLiteConnessione;
import com.matteotocci.app.model.Session;

public class HomePageNutrizionista implements Initializable {

    @FXML
    private Button BottoneDieta;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneRicette; // FXML ID per il bottone Ricette (generico, per i clienti)
    @FXML
    private Button BottoneRicetteNutrizionista; // Nuovo FXML ID se hai un bottone separato per le ricette del nutrizionista
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

    private ObservableList<Cliente> listaClienti = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nomeColonna.setCellValueFactory(new PropertyValueFactory<>("nome"));

        azioniColonna.setCellFactory(new Callback<TableColumn<Cliente, String>, TableCell<Cliente, String>>() {
            @Override
            public TableCell<Cliente, String> call(TableColumn<Cliente, String> param) {
                return new TableCell<Cliente, String>() {
                    final Button visualizzaButton = new Button("Visualizza Dieta");
                    final Button modificaButton = new Button("Modifica Dieta");
                    final HBox container = new HBox(visualizzaButton, modificaButton);
                    {
                        container.setSpacing(5);

                        visualizzaButton.setOnAction(event -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            System.out.println("DEBUG (HomePageNutrizionista): Click su Visualizza Dieta per cliente: " + cliente.getNome() + " (ID: " + cliente.getId() + ")");

                            Dieta dietaAssegnata = recuperaDietaAssegnataACliente(cliente.getId());

                            if (dietaAssegnata != null) {
                                try {
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                                    Parent visualizzaDietaRoot = fxmlLoader.load();

                                    VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();
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
                                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di modifica dieta.", "Verificare il percorso del file FXML.");
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

        setNomeUtenteLabel();
        caricaClientiDelNutrizionista();
    }

    private void setNomeUtenteLabel() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            String nomeUtente = getNomeUtenteDalDatabase(userIdFromSession.toString());
            nomeUtenteLabelHomePage.setText(
                    (nomeUtente != null && !nomeUtente.isEmpty()) ? nomeUtente : "Nome e Cognome"
            );
        } else {
            nomeUtenteLabelHomePage.setText("Nome e Cognome");
            System.err.println("[ERROR - HomePageNutrizionista] ID utente non disponibile dalla Sessione per impostare il nome.");
        }
    }

    private String getNomeUtenteDalDatabase(String userId) {
        String nomeUtente = null;
        // Assicurati che "database.db" sia nel percorso corretto rispetto all'esecuzione dell'applicazione
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
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare il nome utente.", "Dettagli: " + e.getMessage());
        }
        return nomeUtente;
    }

    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String query = "SELECT id, nome_dieta, data_inizio, data_fine, id_nutrizionista, id_cliente " +
                "FROM Diete WHERE id_cliente = ?";

        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dieta = new Dieta(
                        rs.getInt("id"),
                        rs.getString("nome_dieta"),
                        rs.getString("data_inizio"),
                        rs.getString("data_fine"),
                        rs.getInt("id_nutrizionista"),
                        rs.getInt("id_cliente")
                );
                System.out.println("DEBUG (HomePageNutrizionista): Recuperata dieta '" + dieta.getNome() + "' (ID: " + dieta.getId() + ") per cliente ID: " + idCliente);
            } else {
                System.out.println("DEBUG (HomePageNutrizionista): Nessuna dieta trovata per il cliente ID: " + idCliente);
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (HomePageNutrizionista): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile recuperare la dieta.", "Dettagli: " + e.getMessage());
        }
        return dieta;
    }

    private void caricaClientiDelNutrizionista() {
        listaClienti.clear();
        Integer currentNutrizionistaId = Session.getUserId();

        if (currentNutrizionistaId == null) {
            System.err.println("[ERROR - HomePageNutrizionista] ID nutrizionista non disponibile dalla Sessione. Impossibile caricare i clienti.");
            showAlert(Alert.AlertType.WARNING, "Utente non loggato", "ID Nutrizionista non disponibile", "Impossibile caricare la lista clienti. Riprovare il login.");
            return;
        }

        String query = "SELECT u.id, u.Nome, u.Cognome FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ?";
        try (Connection conn = SQLiteConnessione.connector();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, currentNutrizionistaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int clienteId = rs.getInt("id");
                String nome = rs.getString("Nome") + " " + rs.getString("Cognome");
                listaClienti.add(new Cliente(clienteId, nome));
            }
            tabellaClienti.setItems(listaClienti);
        } catch (SQLException e) {
            System.err.println("Errore DB (caricaClienti): " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile caricare i clienti.", "Dettagli: " + e.getMessage());
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
        private int id;

        public Cliente(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    // --- Metodi di Navigazione ---

    @FXML
    private void AccessoDieta(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml"));
            Parent dietaRoot = fxmlLoader.load();
            Stage dietaStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            dietaStage.setScene(new Scene(dietaRoot));
            dietaStage.setTitle("Diete Nutrizionista"); // Titolo più specifico
            dietaStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Diete Nutrizionista'.", "Verificare il percorso del file FXML.");
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml"));
            Parent alimentiRoot = fxmlLoader.load();
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            alimentiStage.setScene(new Scene(alimentiRoot));
            alimentiStage.setTitle("Alimenti"); // Titolo
            alimentiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Alimenti'.", "Verificare il percorso del file FXML.");
        }
    }




    @FXML
    private void AccessoRicetteNutrizionista(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml"));
            Parent ricetteNutrizionistaRoot = fxmlLoader.load();
            Stage ricetteNutrizionistaStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            ricetteNutrizionistaStage.setScene(new Scene(ricetteNutrizionistaRoot));
            ricetteNutrizionistaStage.setTitle("Le Mie Ricette (Nutrizionista)"); // Titolo specifico
            ricetteNutrizionistaStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Le Mie Ricette (Nutrizionista)'.", "Verificare il percorso del file FXML (RicetteNutrizionista.fxml).");
        }
    }


    @FXML
    private void openProfiloNutrizionista(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.setTitle("Profilo Nutrizionista"); // Titolo
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina 'Profilo Nutrizionista'.", "Verificare il percorso del file FXML.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}