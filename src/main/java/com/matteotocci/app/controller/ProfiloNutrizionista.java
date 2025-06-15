package com.matteotocci.app.controller;

import com.matteotocci.app.model.Session;
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

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfiloNutrizionista implements Initializable {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label nomeUtenteSidebarLabel;
    @FXML
    private Button ClientiButton;
    @FXML
    private Button BottoneAlimenti;
    @FXML
    private Button BottoneDieta;
    @FXML
    private Label benvenutoLabel;
    @FXML
    private TextField nomeTextField;
    @FXML
    private TextField cognomeTextField;
    @FXML
    private TextField sessoTextField;
    @FXML
    private TextField dataNascitaTextField;
    @FXML
    private Label modificaPasswordLabel;
    @FXML
    private Button LogoutButton;
    @FXML
    private Label ruoloUtenteLabel;

    // Questo metodo initialize viene chiamato automaticamente dopo che i campi FXML sono stati iniettati.
    // È presente un errore qui: si chiama `initialize()` senza parametri, ma dovrebbe avere `URL url, ResourceBundle resourceBundle`
    // come definito dall'interfaccia Initializable. La versione corretta è quella più in basso.
    @FXML
    public void initialize() { // Questo metodo initialize è errato per l'interfaccia Initializable, dovrebbe avere due parametri.
        inizializzaProfilo(); // Chiama il metodo per inizializzare i dati del profilo.
        ruoloUtenteLabel.setText("Nutrizionista"); // Imposta il testo del ruolo utente a "Nutrizionista".
    }

    private void inizializzaProfilo() { // Metodo privato per inizializzare i campi del profilo con i dati dell'utente.
        Integer userIdFromSession = Session.getUserId(); // Ottiene l'ID dell'utente dalla Sessione.

        if (userIdFromSession != null) { // Controlla se l'ID utente è disponibile.

            // Recupera nome e cognome dell'utente dalla tabella Utente
            String nome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Nome"); // Recupera il nome dell'utente dal database.
            String cognome = getDatoUtenteDalDatabase("Utente", userIdFromSession.toString(), "Cognome"); // Recupera il cognome dell'utente dal database.

            // Imposta il nome completo nella sidebar
            String nomeCompleto = (nome != null ? nome : "") + " " + (cognome != null ? cognome : ""); // Concatena nome e cognome, gestendo i casi null.
            nomeUtenteSidebarLabel.setText(nomeCompleto.trim()); // Imposta il testo della label nella sidebar, rimuovendo spazi extra.

            // Imposta i campi TextField del nome e cognome
            nomeTextField.setText(nome != null ? nome : ""); // Imposta il testo del campo nome.
            cognomeTextField.setText(cognome != null ? cognome : ""); // Imposta il testo del campo cognome.
        }
        else { // Se l'ID utente non è disponibile.
            System.err.println("[ERROR] ID utente non disponibile dalla Sessione. Impossibile recuperare i dati del profilo."); // Stampa un messaggio di errore.
            nomeUtenteSidebarLabel.setText("Utente Sconosciuto"); // Imposta la label della sidebar.
            nomeTextField.setText(""); // Pulisce il campo nome.
            cognomeTextField.setText(""); // Pulisce il campo cognome.
        }
    }

    private String getDatoUtenteDalDatabase(String tabella, String userId, String campo) { // Metodo privato per recuperare un singolo dato utente dal database.
        String valore = null; // Variabile per memorizzare il valore recuperato.
        String url = "jdbc:sqlite:database.db"; // URL del database SQLite.
        String query = "SELECT " + campo + " FROM " + tabella + " WHERE id = ?"; // Query SQL per selezionare un campo da una tabella in base all'ID.

        try (Connection conn = DriverManager.getConnection(url); // Tenta di ottenere una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro della query.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query e ottiene il ResultSet.

            if (rs.next()) { // Se il ResultSet contiene una riga.
                valore = rs.getString(campo); // Ottiene il valore del campo specificato.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("[ERROR] Errore durante la lettura del " + campo + " dalla tabella " + tabella + ": " + e.getMessage()); // Stampa un messaggio di errore.
        }
        return valore; // Restituisce il valore recuperato (o null se non trovato/errore).
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) { // Metodo FXML per navigare alla pagina HomePageNutrizionista (clienti).
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent homePageRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            stage.setScene(new Scene(homePageRoot)); // Imposta la nuova scena.
            stage.show(); // Mostra lo Stage.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }
    @FXML
    private void AccessoDieta(ActionEvent event) { // Metodo FXML per navigare alla pagina DietaNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DietaNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent dietaRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage dietaStage = new Stage(); // Crea un nuovo Stage per la finestra della dieta.
            dietaStage.setScene(new Scene(dietaRoot)); // Imposta la scena per il nuovo Stage.
            dietaStage.setTitle("Diete"); // Imposta il titolo della finestra.
            dietaStage.show(); // Mostra la nuova finestra.
            ((Stage) BottoneDieta.getScene().getWindow()).close(); // Chiude la finestra corrente del profilo.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }


    @FXML
    private void AccessoRicette(ActionEvent event) { // Metodo FXML per navigare alla pagina RicetteNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent root = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            stage.setScene(new Scene(root)); // Imposta la nuova scena.
            stage.show(); // Mostra lo Stage.

        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) { // Metodo FXML per navigare alla pagina AlimentiNutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent loginRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage loginStage = new Stage(); // Crea un nuovo Stage per la finestra degli alimenti.
            loginStage.setScene(new Scene(loginRoot)); // Imposta la scena per il nuovo Stage.
            loginStage.show(); // Mostra la nuova finestra.
            ((Stage) BottoneAlimenti.getScene().getWindow()).close(); // Chiude la finestra corrente del profilo.
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void mostraSchermataModificaPassword(MouseEvent event) { // Metodo FXML per mostrare la schermata di modifica password (attivato da un click del mouse).
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ModificaPassword.fxml")); // Crea un FXMLLoader per caricare il file FXML.
            Parent modificaPasswordRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.

            Stage modificaPasswordStage = new Stage(); // Crea un nuovo Stage per la finestra di modifica password.
            modificaPasswordStage.setTitle("Modifica Password"); // Imposta il titolo della finestra.
            modificaPasswordStage.initOwner(((Node)event.getSource()).getScene().getWindow());

            modificaPasswordStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            modificaPasswordStage.setScene(new Scene(modificaPasswordRoot));

            modificaPasswordStage.setResizable(false); // Rende la finestra non ridimensionabile.
            modificaPasswordStage.setFullScreen(false); // Disabilita la modalità a schermo intero.
            modificaPasswordStage.showAndWait();
        } catch (IOException e) { // Cattura l'IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    public void eseguiLogout(ActionEvent event) { // Metodo pubblico per gestire l'azione di logout.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Crea un Alert di tipo CONFIRMATION.
        alert.setTitle("Conferma Logout"); // Imposta il titolo dell'alert.
        alert.setHeaderText("Sei sicuro di voler uscire?"); // Imposta il testo dell'intestazione dell'alert.
        alert.setContentText("Clicca OK per confermare o Annulla per rimanere."); // Imposta il testo del contenuto dell'alert.
        // Aggiunge il foglio di stile CSS personalizzato all'alert.
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane"); // Applica la classe di stile base al pannello del dialogo.
        alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Applica la classe di stile specifica per gli alert di conferma.

        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta dell'utente (OK o ANNULLA).
        if (result.isPresent() && result.get() == ButtonType.OK) { // Se l'utente ha cliccato OK.
            try { // Inizia un blocco try-catch per gestire l'IOException.
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/PrimaPagina.fxml")); // Carica il file FXML della PrimaPagina (schermata di login o benvenuto iniziale).
                Parent loginRoot = fxmlLoader.load(); // Ottiene il nodo radice dal file FXML caricato.

                // Crea un nuovo Stage (nuova finestra)
                Stage newStage = new Stage(); // Crea una nuova istanza di Stage (una nuova finestra).
                Scene loginScene = new Scene(loginRoot); // Crea una nuova scena con la PrimaPagina come contenuto.

                newStage.setScene(loginScene); // Imposta la scena sul nuovo stage.
                newStage.setTitle("Benvenuto"); // Imposta un titolo a piacere per la nuova finestra.
                newStage.setResizable(false); // Rende la nuova finestra non ridimensionabile.
                newStage.setFullScreen(false); // Non visualizza la nuova finestra a schermo intero.
                newStage.show(); // Mostra la nuova finestra.

                // (Facoltativo) Chiudi la finestra corrente, se vuoi
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); // Chiude la finestra da cui è stato attivato il logout.

            } catch (IOException e) { // Cattura l'eccezione IOException se il caricamento del file FXML fallisce.
                e.printStackTrace(); // Stampa lo stack trace dell'eccezione per debug.
            }

        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Metodo di inizializzazione corretto per l'interfaccia Initializable.
        inizializzaProfilo(); // Chiama il metodo per inizializzare i dati del profilo al caricamento.
        // Nota: La riga 'ruoloUtenteLabel.setText("Nutrizionista");' era duplicata e non era nel initialize corretto.
        // Dovrebbe essere qui o nel CSS se il testo è statico.
    }
}

