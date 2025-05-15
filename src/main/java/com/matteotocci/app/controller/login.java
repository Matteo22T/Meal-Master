package com.matteotocci.app.controller;

import com.matteotocci.app.model.LoginModel;
import com.matteotocci.app.model.Session;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class login implements Initializable {

    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    @FXML private Button btnAccedi;
    @FXML private Button btnRegistrati;

    @FXML private Button BottoneAccedi;
    @FXML private Button BottoneRegistrati;

    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;

    @FXML
    private RadioButton Cliente;

    @FXML
    private RadioButton Nutrizionista;

    @FXML
    private ToggleGroup Ruolo;

    @FXML
    private void switchToLogin() {
        if (!loginBox.isVisible()) {
            fade(registerBox, false);
            fade(loginBox, true);
            highlightButton(btnAccedi, btnRegistrati);
        }
    }

    @FXML
    private void switchToRegister() {
        if (!registerBox.isVisible()) {
            fade(loginBox, false);
            fade(registerBox, true);
            highlightButton(btnRegistrati, btnAccedi);
        }
    }

    private void fade(VBox box, boolean show) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), box);
        if (show) {
            box.setVisible(true);
            box.setOpacity(0);
            ft.setFromValue(0);
            ft.setToValue(1);
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> box.setVisible(false));
        }
        ft.play();
    }

    private void highlightButton(Button active, Button inactive) {
        active.getStyleClass().remove("bottoneSpento");
        inactive.getStyleClass().remove("bottoneAttivo");

        active.getStyleClass().add("bottoneAttivo");
        inactive.getStyleClass().add("bottoneSpento");
    }

    @FXML
    private void AccessoHomePage(ActionEvent event) {
        effettuaLogin();
    }

    @FXML
    private void handleLoginEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            effettuaLogin();
        }
    }

    private void effettuaLogin() {
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        // Controllo campi vuoti
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci email e password.");
            return;
        }

        // Verifica credenziali nel database
        boolean loginRiuscito = loginModel.verificaCredenziali(email, password);

        if (loginRiuscito) {
            String ruolo = loginModel.getRuoloUtente(email);

            if (ruolo == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo non trovato per questo utente.");
                return;
            }
            String fxmlPath;
            if (ruolo.equalsIgnoreCase("nutrizionista")) {
                fxmlPath = "/com/matteotocci/app/HomePageNutrizionista.fxml";
            } else if (ruolo.equalsIgnoreCase("cliente")) {
                fxmlPath = "/com/matteotocci/app/HomePage.fxml";
            } else {
                showAlert(Alert.AlertType.ERROR, "Errore", "Ruolo utente non valido.");
                return;
            }

            showAlert(Alert.AlertType.INFORMATION, "Accesso riuscito", "Benvenuto!");

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent homePageRoot = fxmlLoader.load();

                // **Ottieni il controller HomePage o HomePageNutrizionista**
                int loggedInUserId = loginModel.getIdUtente(email);
                Session.setUserId(loggedInUserId);

                // Passa l'ID utente al controller corretto in base al ruolo
                if (ruolo.equalsIgnoreCase("nutrizionista")) {
                    HomePageNutrizionista homePageController = fxmlLoader.getController();
                    if (homePageController != null && loggedInUserId != -1) {
                        homePageController.setLoggedInUserId(String.valueOf(loggedInUserId));
                    }
                } else if (ruolo.equalsIgnoreCase("cliente")) {
                    HomePage homePageController = fxmlLoader.getController();
                    if (homePageController != null && loggedInUserId != -1) {
                        homePageController.setLoggedInUserId(String.valueOf(loggedInUserId));
                    }
                }

                // Carica la nuova finestra
                Stage homePageStage = new Stage();
                homePageStage.setScene(new Scene(homePageRoot));
                homePageStage.show();
                ((Stage) BottoneAccedi.getScene().getWindow()).close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare la Home Page.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Accesso fallito", "Email o password errati.");
        }
    }

    public LoginModel loginModel = new LoginModel();

    @FXML
    private void Registrato(ActionEvent event) {
        effettuaRegistrazione();
    }

    @FXML
    private void handleRegisterEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            effettuaRegistrazione();
        }
    }

    private void effettuaRegistrazione() {
        // --- Inizio Logica di Registrazione ---
        if (nomeField == null || cognomeField == null || emailField == null || passwordField == null || Ruolo == null) {
            System.err.println("Errore: Campi FXML non inizializzati nel controller!");
            showAlert(Alert.AlertType.ERROR, "Errore Interno", "Errore nell'interfaccia utente.");
            return;
        }

        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        boolean campiValidi = true;
        String messaggioErrore = "";

        // Validazione campi vuoti e formato
        if (nome.isEmpty()) {
            messaggioErrore += "Il campo Nome è obbligatorio.\n";
            campiValidi = false;
        }
        if (cognome.isEmpty()) {
            messaggioErrore += "Il campo Cognome è obbligatorio.\n";
            campiValidi = false;
        }
        if (email.isEmpty()) {
            messaggioErrore += "Il campo Email è obbligatorio.\n";
            campiValidi = false;
        } else if (!email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            messaggioErrore += "L'email deve essere un indirizzo @gmail.com valido.\n";
            campiValidi = false;
        }
        if (password.isEmpty()) {
            messaggioErrore += "Il campo Password è obbligatorio.\n";
            campiValidi = false;
        } else if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            messaggioErrore += "La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero.\n";
            campiValidi = false;
        }

        Toggle selectedToggle = Ruolo.getSelectedToggle();
        if (selectedToggle == null) {
            messaggioErrore += "Seleziona un ruolo (Cliente o Nutrizionista).\n";
            campiValidi = false;
        }

        if (!campiValidi) {
            showAlert(Alert.AlertType.ERROR, "Registrazione Incompleta", messaggioErrore);
            return;
        }

        RadioButton selectedRadioButton = (RadioButton) selectedToggle;
        String ruolo = selectedRadioButton.getText().toLowerCase();

        boolean successo = loginModel.registraUtente(nome, cognome, email, password, ruolo);

        // --- Fine Logica di Registrazione ---

        if (successo) {
            int idUtente = loginModel.getIdUtente(email);
            if (idUtente == -1) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile recuperare l'ID dell'utente.");
                return;
            }

            try {
                FXMLLoader fxmlLoader;
                Parent root;
                if (ruolo.equals("nutrizionista")) {
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ConfermaRegistrazione.fxml"));
                    root = fxmlLoader.load();
                } else {
                    fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/DatiCliente.fxml"));
                    root = fxmlLoader.load();
                    DatiCliente datiClienteController = fxmlLoader.getController();
                    if (datiClienteController != null) {
                        datiClienteController.setIdUtente(idUtente);
                    } else {
                        System.err.println("Errore: Controller non caricato correttamente.");
                        showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare il controller.");
                        return;
                    }
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Benvenuto!");
                stage.show();

                if (BottoneRegistrati != null && BottoneRegistrati.getScene() != null && BottoneRegistrati.getScene().getWindow() != null) {
                    ((Stage) BottoneRegistrati.getScene().getWindow()).close();
                } else {
                    System.err.println("Impossibile ottenere la finestra corrente per chiuderla.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina successiva dopo la registrazione.");
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Errore di registrazione", "Impossibile registrare l'utente. L'email potrebbe essere già in uso o si è verificato un problema.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loginModel.isDbConnected()){
            System.out.println("Connected");
        } else{
            System.out.println("Not connected");
        }

        loginPasswordField.setOnKeyPressed(this::handleLoginEnter);
        loginEmailField.setOnKeyPressed(this::handleLoginEnter);

        nomeField.setOnKeyPressed(this::handleRegisterEnter);
        cognomeField.setOnKeyPressed(this::handleRegisterEnter);
        emailField.setOnKeyPressed(this::handleRegisterEnter);
        passwordField.setOnKeyPressed(this::handleRegisterEnter);
    }
}
