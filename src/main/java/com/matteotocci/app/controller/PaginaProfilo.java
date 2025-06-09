package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.Dieta; // Importa la classe Dieta
import com.matteotocci.app.model.SQLiteConnessione; // Importa la classe SQLiteConnessione

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaginaProfilo implements Initializable {

    @FXML
    private ImageView profileImage;

    @FXML
    private Label nomeUtenteSidebarLabel;

    @FXML
    private Label benvenutoLabel;

    @FXML
    private ImageView ImmagineOmino;

    @FXML
    private TextField nomeTextField;

    @FXML
    private TextField cognomeTextField;

    @FXML
    private TextField sessoTextField;

    @FXML
    private TextField dataNascitaTextField;

    @FXML
    private TextField altezzaTextField;

    @FXML
    private TextField pesoAttualeTextField;

    @FXML
    private TextField pesoIdealeTextField;

    @FXML
    private TextField dietaTextField;

    @FXML
    private TextField livelloAttivitaTextField;

    @FXML
    private TextField nutrizionistaTextField;

    @FXML
    private GridPane gridPane;

    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneRicette;
    @FXML
    private Button homePageButton;
    @FXML
    private Button LogoutButton;
    @FXML
    private Button BottonePiano; // Assicurati di avere questo fx:id nel tuo FXML per il bottone del piano alimentare

    // Dichiarazione della variabile per la dieta assegnata
    private Dieta dietaAssegnata;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("[DEBUG] initialize chiamato in PaginaProfilo.");
        // Chiama il metodo per caricare i dati utente non appena il controller è pronto
        inizializzaDatiUtente();
        // Recupera la dieta assegnata all'avvio della pagina
        recuperaEImpostaDietaAssegnata();
    }

    private void inizializzaDatiUtente() {
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID direttamente dalla Sessione

        if (userIdFromSession != null) {
            System.out.println("[DEBUG] Tentativo di recupero dati per l'utente con ID: " + userIdFromSession);

            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome");
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome");

            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : "");
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim());
            benvenutoLabel.setText("Benvenuto " + nomeCompleto.trim());

            nomeTextField.setText(nome != null ? nome : "");
            cognomeTextField.setText(cognome != null ? cognome : "");

            String altezza = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "altezza_cm");
            String peso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "peso_kg");
            String livelloAttivita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "livello_attivita");
            String dataNascita = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "data_di_nascita");
            String sesso = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "sesso");

            if (altezzaTextField != null) {
                altezzaTextField.setText(altezza != null ? altezza : "");
            }
            if (pesoAttualeTextField != null) {
                pesoAttualeTextField.setText(peso != null ? peso : "");
            }
            if (livelloAttivitaTextField != null) {
                livelloAttivitaTextField.setText(livelloAttivita != null ? livelloAttivita : "");
            }
            if (dataNascitaTextField != null) {
                dataNascitaTextField.setText(dataNascita != null ? dataNascita : "");
            }
            if (sessoTextField != null) {
                sessoTextField.setText(sesso != null ? sesso : "");
            }
            if (nutrizionistaTextField != null) {
                String idNutrizionista = getDatoUtenteDalDatabase("Clienti", userIdFromSession.toString(), "id_nutrizionista");

                if (idNutrizionista != null && !idNutrizionista.isEmpty()) {
                    String nomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Nome");
                    String cognomeNutrizionista = getDatoUtenteDalDatabase("Utente", idNutrizionista, "Cognome");

                    if (nomeNutrizionista != null && cognomeNutrizionista != null) {
                        nutrizionistaTextField.setText(nomeNutrizionista + " " + cognomeNutrizionista);
                    } else {
                        nutrizionistaTextField.setText("Nutrizionista non trovato");
                    }
                } else {
                    nutrizionistaTextField.setText("Non assegnato");
                }
            }

        } else {
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo.");
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto");
            benvenutoLabel.setText("Benvenuto Utente");
            nomeTextField.setText("");
            cognomeTextField.setText("");
            if (sessoTextField != null) sessoTextField.setText("");
            if (dataNascitaTextField != null) dataNascitaTextField.setText("");
            if (altezzaTextField != null) altezzaTextField.setText("");
            if (pesoAttualeTextField != null) pesoAttualeTextField.setText("");
            if (pesoIdealeTextField != null) pesoIdealeTextField.setText("");
            if (dietaTextField != null) dietaTextField.setText("");
            if (livelloAttivitaTextField != null) livelloAttivitaTextField.setText("");
            if (nutrizionistaTextField != null) nutrizionistaTextField.setText("");
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) {
        String valore = null;
        String url = "jdbc:sqlite:database.db";
        String query;
        String idColumn = "id";
        if (tabella.equals("Clienti")) {
            idColumn = "id_cliente";
        }
        query = "SELECT " + campo + " FROM " + tabella + " WHERE " + idColumn + " = ?";
        System.out.println("[DEBUG] Query per " + tabella + " eseguita: " + query + " con ID: " + userId + ", Campo: " + campo);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                valore = rs.getString(campo);
                System.out.println("[DEBUG] Valore recuperato per " + campo + " da " + tabella + ": " + valore);
            } else {
                System.out.println("[DEBUG] Nessun dato trovato con ID: " + userId + " nella tabella " + tabella + " per il campo " + campo);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage());
        }
        return valore;
    }

    // --- Metodi di Navigazione ---

    @FXML
    private void AccessoProfilo(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PaginaProfilo.fxml"));
            Parent profileRoot = fxmlLoader.load();
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            profileStage.setScene(new Scene(profileRoot));
            profileStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Alimenti.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/Ricette.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml"));
            Parent modificaPasswordRoot = fxmlLoader.load();

            ModificaPassword modificaPasswordController = fxmlLoader.getController();

            Integer userId = Session.getUserId();
            if (userId != null) {
                modificaPasswordController.setUtenteCorrenteId(userId.toString());
            } else {
                System.err.println("[ERROR - PaginaProfilo] ID utente non disponibile dalla Sessione per ModificaPassword.");
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile modificare la password", "ID utente non disponibile. Riprova il login.");
            }

            Stage modificaPasswordStage = new Stage();
            modificaPasswordStage.setTitle("Modifica Password");
            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));
            modificaPasswordStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di modifica password.", "Verificare il percorso del file FXML.");
        }
    }

    @FXML
    private void vaiAllaHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la HomePage.", "Contattare l'amministratore.");
        }
    }

    @FXML
    private void eseguiLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Logout");
        alert.setHeaderText("Sei sicuro di voler uscire?");
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml"));
                Parent loginRoot = fxmlLoader.load();
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene loginScene = new Scene(loginRoot);
                currentStage.setScene(loginScene);
                currentStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Logout", "Impossibile effettuare il logout.", "Contattare l'amministratore.");
            }
        }
    }
    @FXML
    private void mostraBMI(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/BMI.fxml"));
            Parent bmiRoot = fxmlLoader.load();

            BMI bmiController = fxmlLoader.getController();

            Integer userId = Session.getUserId();
            if (userId != null) {
                bmiController.setUtenteCorrenteId(userId.toString());
            } else {
                System.err.println("[ERROR - PaginaProfilo] ID utente non disponibile dalla Sessione per BMI.");
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile calcolare il BMI", "ID utente non disponibile. Riprova il login.");
            }

            Stage bmiStage = new Stage();
            bmiStage.setTitle("Calcolo BMI");
            bmiStage.setScene(new Scene(bmiRoot));
            bmiStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata di calcolo BMI.", "Verificare il percorso del file FXML.");
        }
    }

    // Metodo per recuperare la dieta assegnata al cliente (copiato da Ricette)
    private Dieta recuperaDietaAssegnataACliente(int idCliente) {
        Dieta dieta = null;
        String url = "jdbc:sqlite:database.db"; // Assicurati che sia il percorso corretto
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
            }
        } catch (SQLException e) {
            System.err.println("ERRORE SQL (PaginaProfilo): Errore durante il recupero della dieta per il cliente: " + e.getMessage());
            e.printStackTrace();
        }
        return dieta;
    }

    // Metodo per recuperare e impostare la dieta all'inizializzazione (copiato da Ricette)
    private void recuperaEImpostaDietaAssegnata() {
        Integer userIdFromSession = Session.getUserId();
        if (userIdFromSession != null) {
            this.dietaAssegnata = recuperaDietaAssegnataACliente(userIdFromSession.intValue());
            if (this.dietaAssegnata != null) {
                System.out.println("DEBUG (PaginaProfilo): Dieta '" + dietaAssegnata.getNome() + "' (ID: " + dietaAssegnata.getId() + ") recuperata per utente ID: " + userIdFromSession);
                // Puoi anche impostare un TextField sulla pagina del profilo per mostrare il nome della dieta, se presente
                if (dietaTextField != null) {
                    dietaTextField.setText(dietaAssegnata.getNome());
                }
            } else {
                System.out.println("DEBUG (PaginaProfilo): Nessuna dieta trovata per l'utente ID: " + userIdFromSession);
                if (dietaTextField != null) {
                    dietaTextField.setText("Nessuna dieta assegnata");
                }
            }
        } else {
            System.err.println("[ERROR - PaginaProfilo] ID utente non disponibile dalla Sessione per recupero dieta.");
            if (dietaTextField != null) {
                dietaTextField.setText("Errore caricamento dieta");
            }
        }
    }

    // Metodo per visualizzare gli alert
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void AccessoPianoAlimentare(ActionEvent event) {
        if (dietaAssegnata != null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/VisualizzaDieta.fxml"));
                Parent visualizzaDietaRoot = fxmlLoader.load();

                VisualizzaDieta visualizzaDietaController = fxmlLoader.getController();

                // Passa l'oggetto Dieta recuperato al controller della pagina di visualizzazione
                visualizzaDietaController.impostaDietaDaVisualizzare(dietaAssegnata);
                System.out.println("DEBUG (PaginaProfilo): Passato Dieta ID " + dietaAssegnata.getId() + " al controller VisualizzaDieta.");

                Stage dietaStage = new Stage();
                dietaStage.setScene(new Scene(visualizzaDietaRoot));
                dietaStage.show();

            } catch (IOException e) {
                System.err.println("ERRORE (PaginaProfilo): Errore caricamento FXML VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile aprire la schermata della dieta.", "Verificare il percorso del file FXML.");
            } catch (Exception e) {
                System.err.println("ERRORE (PaginaProfilo): Errore generico durante l'apertura di VisualizzaDieta: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore inatteso.", "Dettagli: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG (PaginaProfilo): Nessuna dieta trovata per il cliente (ID: " + Session.getUserId() + ").");
            showAlert(Alert.AlertType.INFORMATION, "Nessuna Dieta", "Nessuna dieta assegnata",
                    "Il cliente non ha diete assegnate o non è stato possibile recuperarle.");
        }
    }
}