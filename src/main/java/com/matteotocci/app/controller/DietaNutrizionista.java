package com.matteotocci.app.controller;

import com.matteotocci.app.model.Dieta;
import com.matteotocci.app.model.Session;
import com.matteotocci.app.model.SQLiteConnessione;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;


import java.io.IOException; // Importa IOException per la gestione degli errori I/O.
import java.net.URL; // Importa URL (necessario per Initializable).
import java.sql.*; // Importa tutte le classi SQL (Connection, PreparedStatement, ResultSet, SQLException).
import java.util.*; // Importa tutte le utility di Java (Map, HashMap, List, ArrayList, Optional).
import java.util.stream.Collectors; // Importa Collectors per l'utilizzo degli stream API.

// La classe deve implementare Initializable
public class DietaNutrizionista implements Initializable { // Dichiara la classe DietaNutrizionista e implementa Initializable.

    @FXML
    private Button BottoneClienti; // Bottone FXML per navigare alla sezione Clienti.
    @FXML
    private Button BottoneAlimenti; // Bottone FXML per navigare alla sezione Alimenti.
    @FXML
    private Button BottoneDiete; // Bottone FXML per navigare alla sezione Diete (presumibilmente questa stessa pagina).
    @FXML
    private Button BottoneRicette; // Aggiunto per coerenza con le altre pagine: Bottone FXML per navigare alla sezione Ricette.

    @FXML
    private Label nomeUtenteLabelDieta; // Label FXML per visualizzare il nome dell'utente loggato.

    @FXML
    private ListView<Dieta> listaDieteAssegnate; // ListView FXML per mostrare le diete già assegnate.
    @FXML
    private ListView<Dieta> listaDieteDaAssegnare; // ListView FXML per mostrare le diete disponibili da assegnare.

    @FXML
    private TextField filtroNomeDietaTextField; // TextField FXML per filtrare le diete per nome.

    @FXML
    private VBox contenitorePrincipale; // VBox FXML che funge da contenitore principale del layout.


    private ObservableList<Dieta> observableListaDieteAssegnate = FXCollections.observableArrayList(); // ObservableList per le diete assegnate (sincronizzata con la ListView).
    private ObservableList<Dieta> observableListaDieteDaAssegnare = FXCollections.observableArrayList(); // ObservableList per le diete da assegnare (sincronizzata con la ListView).

    private Map<String, Integer> clientiMap = new HashMap<>(); // Mappa per memorizzare i nomi dei clienti e i loro ID.
    private VBox sezioneClientiAssegnazione = null; // VBox per la sezione di selezione dei clienti per l'assegnazione.

    // Questo è il metodo initialize corretto per l'interfaccia Initializable
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { // Implementazione del metodo initialize, chiamato all'avvio del controller.
        listaDieteAssegnate.setItems(observableListaDieteAssegnate); // Collega la ObservableList alle diete assegnate alla ListView.
        listaDieteDaAssegnare.setItems(observableListaDieteDaAssegnare); // Collega la ObservableList alle diete da assegnare alla ListView.

        ConfigurazioneCelle(listaDieteAssegnate, true); // Configura il rendering delle celle per la lista delle diete assegnate.
        ConfigurazioneCelle(listaDieteDaAssegnare, false); // Configura il rendering delle celle per la lista delle diete da assegnare.

        filtroNomeDietaTextField.textProperty().addListener((observable, oldValue, newValue) -> filtraDiete(newValue)); // Aggiunge un listener al campo di testo del filtro per aggiornare le diete in tempo reale.

        listaDieteAssegnate.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> { // Aggiunge un listener per la selezione nella lista delle diete assegnate.
            if (newSelection != null) { // Se è stata fatta una nuova selezione.
                listaDieteDaAssegnare.getSelectionModel().clearSelection(); // Deseleziona qualsiasi elemento nella lista "Da Assegnare".
                rimuoviSezioneClientiAssegnazione(); // Rimuove la sezione di assegnazione clienti se visibile.
            }
        });

        listaDieteDaAssegnare.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> { // Aggiunge un listener per la selezione nella lista delle diete da assegnare.
            if (newSelection == null && oldSelection != null) { // Se la selezione è stata rimossa (l'utente ha deselezionato).
                rimuoviSezioneClientiAssegnazione(); // Rimuove la sezione di assegnazione clienti se visibile.
            }
        });

        // CHIAMATE ALL'INIZIALIZZAZIONE ALL'AVVIO DEL CONTROLLER
        // Ora il controller è pronto e recupera l'ID e il ruolo dalla Session
        setNomeUtenteLabel(); // Chiama il metodo per impostare il nome dell'utente.
        caricaListaDiete(); // Chiama il metodo per caricare le liste delle diete dal database.
    }

    private void setNomeUtenteLabel() { // Metodo privato per impostare il nome dell'utente loggato nella label.
        Integer userIdFromSession = Session.getUserId(); // Prende l'ID dell'utente direttamente dalla Session.

        if (nomeUtenteLabelDieta != null  && userIdFromSession != null) { // Se la label esiste e l'ID utente non è nullo.
            String nomeUtenteCompleto = getNomeUtenteDalDatabase(userIdFromSession.toString()); // Recupera il nome completo dell'utente dal database.
            nomeUtenteLabelDieta.setText((nomeUtenteCompleto != null && !nomeUtenteCompleto.isEmpty()) ? nomeUtenteCompleto : "Nome e Cognome"); // Imposta il testo della label con il nome recuperato o un fallback.
        } else { // Se la label non esiste o l'ID utente è nullo.
            nomeUtenteLabelDieta.setText("Nome e Cognome"); // Fallback: Imposta un testo di default.
            System.err.println("[ERROR - DietaNutrizionista] Impossibile impostare il nome/ruolo utente. Componenti UI o ID/ruolo dalla Sessione sono null."); // Stampa un messaggio di errore.
        }
    }

    private String getNomeUtenteDalDatabase(String userId) { // Metodo privato per recuperare il nome completo dell'utente dal database.
        String nomeUtenteCompleto = null; // Variabile per il nome completo, inizializzata a null.
        String url = "jdbc:sqlite:database.db"; // Assicurati che il percorso del database sia corretto: URL del database SQLite.
        String query = "SELECT Nome, Cognome FROM Utente WHERE id = ?"; // Query SQL per selezionare nome e cognome dall'utente.
        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.
            pstmt.setString(1, userId); // Imposta l'ID utente come parametro.
            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            if (rs.next()) { // Se un risultato è presente.
                nomeUtenteCompleto = rs.getString("Nome") + " " + rs.getString("Cognome"); // Concatena nome e cognome.
            }
        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("Errore DB (nome utente): " + e.getMessage()); // Stampa un messaggio di errore.
        }
        return nomeUtenteCompleto; // Restituisce il nome completo.
    }

    private void caricaListaDiete() { // Metodo privato per caricare le diete dal database e popolarle nelle liste.
        observableListaDieteAssegnate.clear(); // Pulisce la lista delle diete assegnate.
        observableListaDieteDaAssegnare.clear(); // Pulisce la lista delle diete da assegnare.

        Integer currentNutrizionistaId = Session.getUserId(); // Ottieni l'ID del nutrizionista dalla Session.

        if (currentNutrizionistaId == null) { // Se l'ID del nutrizionista non è disponibile.
            System.err.println("[ERROR - DietaNutrizionista] ID nutrizionista non disponibile. Impossibile caricare le diete."); // Stampa un messaggio di errore.
            return; // Esce dal metodo.
        }

        String url = "jdbc:sqlite:database.db"; // URL del database SQLite.
        String query = "SELECT d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente, COUNT(gd.id_giorno_dieta) AS numero_giorni " + // Query SQL per selezionare i dettagli delle diete.
                "FROM Diete d " +
                "LEFT JOIN Giorno_dieta gd ON d.id = gd.id_dieta " +
                "WHERE d.id_nutrizionista = ? " +
                "GROUP BY d.id, d.nome_dieta, d.data_inizio, d.data_fine, d.id_cliente"; // Raggruppa per id della dieta.

        try (Connection conn = DriverManager.getConnection(url); // Ottiene una connessione al database.
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepara lo statement SQL.

            pstmt.setInt(1, currentNutrizionistaId); // Usa l'ID Integer direttamente: Imposta l'ID del nutrizionista come parametro.

            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            while (rs.next()) { // Itera su ogni riga del ResultSet.
                int idDieta = rs.getInt("id"); // Ottiene l'ID della dieta.
                String nomeDieta = rs.getString("nome_dieta"); // Ottiene il nome della dieta.
                String dataInizio = rs.getString("data_inizio"); // Ottiene la data di inizio.
                String dataFine = rs.getString("data_fine"); // Ottiene la data di fine.
                Object idClienteObj = rs.getObject("id_cliente"); // Ottiene l'ID del cliente come oggetto (può essere NULL).
                int numeroGiorni = rs.getInt("numero_giorni"); // Ottiene il numero di giorni.

                Dieta dieta = new Dieta(idDieta, nomeDieta, dataInizio, dataFine); // Crea un nuovo oggetto Dieta.
                dieta.setNumeroGiorni(numeroGiorni); // Imposta il numero di giorni.
                if (idClienteObj != null) { // Se l'ID del cliente non è nullo.
                    dieta.setIdCliente((Integer) idClienteObj); // Imposta l'ID del cliente.
                } else { // Se l'ID del cliente è nullo.
                    dieta.setIdCliente(0); // Nessun cliente assegnato: Imposta 0 come ID cliente (per indicare non assegnato).
                }

                if (dieta.getIdCliente() == 0) { // Se la dieta non è assegnata.
                    observableListaDieteDaAssegnare.add(dieta); // Aggiunge alla lista delle diete da assegnare.
                } else { // Se la dieta è assegnata.
                    observableListaDieteAssegnate.add(dieta); // Aggiunge alla lista delle diete assegnate.
                }
            }
        } catch (SQLException e) { // Cattura le eccezioni SQL.
            System.err.println("Errore DB (carica diete): " + e.getMessage()); // Stampa un messaggio di errore.
        }
    }

    private void ConfigurazioneCelle(ListView<Dieta> listView, boolean ListaAssegnata) { // Metodo privato per configurare le celle delle ListView.
        listView.setCellFactory(lv -> new ListCell<Dieta>() { // Imposta una CellFactory personalizzata per la ListView.
            private final HBox hbox = new HBox(10); // Crea un HBox per il layout interno della cella con spaziatura 10.
            private final Label nomeLabel = new Label(); // Crea una Label per il nome della dieta.
            private final Button btnModifica = new Button("Modifica Piano"); // Crea un bottone "Modifica Piano".
            private final Button btnAssegna = new Button("Assegna Piano"); // Crea un bottone "Assegna Piano".
            private final Button btnAnnullaAssegnazione = new Button("Rimuovi"); // Crea un bottone "Rimuovi" (per annullare assegnazione).

            { // Blocco di inizializzazione dell'istanza (viene eseguito per ogni nuova cella).
                hbox.getChildren().addAll(nomeLabel, btnModifica); // Aggiunge nome e bottone Modifica all'HBox.
                if (!ListaAssegnata) { // Aggiungi il bottone Assegna solo per la lista "Da Assegnare"
                    hbox.getChildren().add(btnAssegna); // Aggiunge il bottone Assegna se la lista non è assegnata.
                } else { // Aggiungi il bottone Annulla Assegnazione solo per la lista "Assegnate"
                    hbox.getChildren().add(btnAnnullaAssegnazione); // Aggiunge il bottone Annulla Assegnazione se la lista è assegnata.
                }

                // Stile dei bottoni
                btnModifica.getStyleClass().add("button-primary"); // Aggiunge la classe CSS "button-primary" al bottone Modifica.
                btnAssegna.getStyleClass().add("button-primary"); // Aggiunge la classe CSS "button-primary" al bottone Assegna.
                btnAnnullaAssegnazione.getStyleClass().add("button-danger"); // Aggiunge la classe CSS "button-danger" al bottone Rimuovi.

                btnModifica.setOnAction(event -> { // Imposta l'azione per il bottone Modifica.
                    Dieta dieta = getItem(); // Ottiene l'oggetto Dieta associato a questa cella.
                    if (dieta != null) { // Se la dieta non è nulla.
                        apriFinestraModificaGiornoDieta(dieta, ((Node) event.getSource()).getScene().getWindow()); // Apre la finestra di modifica.
                    }
                });

                btnAssegna.setOnAction(event -> { // Imposta l'azione per il bottone Assegna.
                    Dieta dieta = getItem(); // Ottiene l'oggetto Dieta associato a questa cella.
                    if (dieta != null) { // Se la dieta non è nulla.
                        listaDieteDaAssegnare.getSelectionModel().select(dieta); // Seleziona la dieta nella lista "Da Assegnare".
                        mostraSelezioneClienti(dieta); // Mostra la sezione per la selezione dei clienti.
                    }
                });

                btnAnnullaAssegnazione.setOnAction(event -> { // Imposta l'azione per il bottone Annulla Assegnazione.
                    Dieta dieta = getItem(); // Ottiene l'oggetto Dieta associato a questa cella.
                    if (dieta != null) { // Se la dieta non è nulla.
                        annullaAssegnazioneDieta(dieta); // Annulla l'assegnazione della dieta.
                    }
                });
            }

            @Override
            protected void updateItem(Dieta dieta, boolean empty) { // Metodo chiamato per aggiornare il contenuto della cella.
                super.updateItem(dieta, empty); // Chiama il metodo super.

                if (empty || dieta == null) { // Se la cella è vuota o la dieta è nulla.
                    setText(null); // Imposta il testo a null.
                    setGraphic(null); // Rimuove il graphic.
                } else { // Se la cella contiene un elemento.
                    nomeLabel.setText(dieta.getNome()); // Imposta il testo della label con il nome della dieta.
                    setGraphic(hbox); // Imposta l'HBox come graphic della cella.
                }
            }
        });
    }

    private void rimuoviSezioneClientiAssegnazione() { // Metodo privato per rimuovere la sezione di selezione clienti dall'interfaccia.
        if (sezioneClientiAssegnazione != null && contenitorePrincipale.getChildren().contains(sezioneClientiAssegnazione)) { // Se la sezione esiste e si trova nel contenitore principale.
            contenitorePrincipale.getChildren().remove(sezioneClientiAssegnazione); // La rimuove.
            sezioneClientiAssegnazione = null; // Resetta il riferimento.
        }
    }

    private void mostraSelezioneClienti(Dieta dieta) { // Metodo privato per mostrare la sezione di selezione clienti.
        rimuoviSezioneClientiAssegnazione(); // Rimuove qualsiasi sezione clienti preesistente.

        // Blocco 1: Se la dieta selezionata è già assegnata, non mostrare la selezione del cliente.
        if (dieta.getIdCliente() != 0) { // Un id_cliente diverso da 0 significa che è assegnata: Controlla se la dieta è già assegnata a un cliente.
            showAlert(Alert.AlertType.WARNING,"Attenzione", "Questa dieta è già assegnata. Annulla prima l'assegnazione corrente se vuoi cambiarla."); // Mostra un avviso.
            return; // Esce dal metodo.
        }

        // Carica i clienti disponibili (cioè quelli senza una dieta assegnata)
        List<String> clientiDisponibili = caricaClienti(); // Questo metodo ora filtra già i clienti con dieta: Carica la lista dei clienti disponibili.

        // Blocco 2: Se non ci sono clienti disponibili per l'assegnazione
        if (clientiDisponibili.isEmpty()) { // Se la lista dei clienti disponibili è vuota.
            showAlert(Alert.AlertType.WARNING,"Attenzione", "Nessun cliente disponibile per l'assegnazione di una nuova dieta. Tutti i clienti hanno già una dieta assegnata."); // Mostra un avviso.
            return; // Esce dal metodo.
        }

        // Se ci sono clienti disponibili e la dieta non è già assegnata, procedi con la creazione dell'interfaccia
        sezioneClientiAssegnazione = new VBox(5); // Crea una nuova VBox con spaziatura 5.
        sezioneClientiAssegnazione.setPadding(new Insets(15)); // Imposta il padding.
        sezioneClientiAssegnazione.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-radius: 5px;"); // Imposta lo stile CSS.

        ToggleGroup toggleGroup = new ToggleGroup(); // Crea un nuovo ToggleGroup per i RadioButton.
        // clientiMap viene riempito da caricaClienti() // Commento che indica dove viene popolata la mappa clienti.

        sezioneClientiAssegnazione.getChildren().add(new Label("Seleziona un cliente:")); // Aggiunge una label.
        sezioneClientiAssegnazione.getChildren().add(new Separator()); // Aggiunge un separatore.

        for (Map.Entry<String, Integer> entry : clientiMap.entrySet()) { // Itera su ogni cliente nella mappa.
            RadioButton rb = new RadioButton(entry.getKey()); // Crea un RadioButton con il nome del cliente.
            rb.setToggleGroup(toggleGroup); // Aggiunge il RadioButton al ToggleGroup.
            sezioneClientiAssegnazione.getChildren().add(rb); // Aggiunge il RadioButton alla VBox.
        }

        Button btnConferma = new Button("Conferma Assegnazione"); // Crea il bottone di conferma.
        btnConferma.setDisable(true); // Lo disabilita inizialmente.
        btnConferma.getStyleClass().add("button-positive"); // Aggiunge la classe CSS.

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> { // Aggiunge un listener al cambio di selezione del ToggleGroup.
            btnConferma.setDisable(newVal == null); // Abilita/disabilita il bottone di conferma in base alla selezione.
        });

        btnConferma.setOnAction(event -> { // Imposta l'azione per il bottone di conferma.
            RadioButton selected = (RadioButton) toggleGroup.getSelectedToggle(); // Ottiene il RadioButton selezionato.
            if (selected != null) { // Se un RadioButton è selezionato.
                String selectedCliente = selected.getText().trim(); // Ottiene il nome del cliente selezionato.
                assegnaDietaACliente(dieta, selectedCliente); // Questo metodo ora aggiorna la dieta nel DB: Chiama il metodo per assegnare la dieta al cliente.
                rimuoviSezioneClientiAssegnazione(); // Rimuovi la sezione dopo l'assegnazione: Rimuove la sezione di selezione clienti.
            }
        });

        sezioneClientiAssegnazione.getChildren().add(btnConferma); // Aggiunge il bottone di conferma alla VBox.
        contenitorePrincipale.getChildren().add(sezioneClientiAssegnazione); // Aggiunge la sezione clienti al contenitore principale.
    }

    private void assegnaDietaACliente(Dieta dieta, String nomeCliente) { // Metodo privato per assegnare una dieta a un cliente nel database.
        Integer idCliente = clientiMap.get(nomeCliente); // Ottiene l'ID del cliente dal nome.

        if (idCliente == null) { // Se l'ID del cliente è nullo.
            showAlert(Alert.AlertType.ERROR,"Errore", "Cliente non valido."); // Mostra un avviso di errore.
            return; // Esce dal metodo.
        }

        try (Connection conn = SQLiteConnessione.connector()) { // Ottiene una connessione al database.
            String sql = "UPDATE Diete SET id_cliente = ? WHERE id = ?"; // Query SQL per aggiornare l'ID del cliente nella tabella Diete.
            PreparedStatement ps = conn.prepareStatement(sql); // Prepara lo statement.
            ps.setInt(1, idCliente); // Imposta l'ID del cliente come parametro.
            ps.setInt(2, dieta.getId()); // Imposta l'ID della dieta come parametro.

            int affected = ps.executeUpdate(); // Esegue l'aggiornamento e ottiene il numero di righe influenzate.
            if (affected > 0) { // Se almeno una riga è stata aggiornata.
                showAlert(Alert.AlertType.INFORMATION,"Successo", "Dieta assegnata correttamente."); // Mostra un avviso di successo.
                caricaListaDiete(); // Ricarica le liste dopo l'assegnazione.
            } else { // Se nessuna riga è stata aggiornata.
                showAlert(Alert.AlertType.ERROR,"Errore", "Nessuna dieta aggiornata."); // Mostra un avviso di errore.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL.
            e.printStackTrace(); // Stampa lo stack trace.
            showAlert(Alert.AlertType.ERROR,"Errore", "Errore durante l'assegnazione: " + e.getMessage()); // Mostra un avviso di errore.
        }
    }

    private void annullaAssegnazioneDieta(Dieta dieta) { // Metodo privato per annullare l'assegnazione di una dieta.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Crea un Alert di tipo CONFIRMATION.
        alert.setTitle("Conferma Annullamento Assegnazione"); // Imposta il titolo.
        alert.setHeaderText("Sei sicuro di voler annullare l'assegnazione della dieta \"" + dieta.getNome() + "\"?"); // Imposta l'header text con il nome della dieta.
        alert.setContentText("Questa azione renderà la dieta nuovamente disponibile per l'assegnazione."); // Imposta il messaggio di contenuto.
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm()); // Aggiunge il CSS.
        alert.getDialogPane().getStyleClass().add("dialog-pane"); // Aggiunge la classe CSS base.
        alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge la classe CSS specifica.


        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende la risposta dell'utente.
        if (result.isPresent() && result.get() == ButtonType.OK) { // Se l'utente ha cliccato OK.
            String url = "jdbc:sqlite:database.db"; // Assicurati che il percorso del database sia corretto: URL del database.
            String sql = "UPDATE Diete SET id_cliente = NULL WHERE id = ?"; // Query SQL per impostare id_cliente a NULL.

            try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione.
                 PreparedStatement ps = conn.prepareStatement(sql)) { // Prepara lo statement.

                ps.setInt(1, dieta.getId()); // Imposta l'ID della dieta.

                int affectedRows = ps.executeUpdate(); // Esegue l'aggiornamento.
                if (affectedRows > 0) { // Se righe sono state influenzate.
                    showAlert(Alert.AlertType.INFORMATION,"Successo", "Assegnazione della dieta annullata correttamente."); // Mostra successo.
                    caricaListaDiete(); // Ricarica le liste.
                } else { // Se nessuna riga è stata influenzata.
                    showAlert(Alert.AlertType.ERROR,"Errore", "Nessuna dieta aggiornata. L'annullamento dell'assegnazione potrebbe non essere avvenuto."); // Mostra errore.
                }

            } catch (SQLException e) { // Cattura eccezioni SQL.
                e.printStackTrace(); // Stampa stack trace.
                showAlert(Alert.AlertType.ERROR,"Errore DB", "Errore durante l'annullamento dell'assegnazione: " + e.getMessage()); // Mostra errore.
            }
        }
    }

    private List<String> caricaClienti() { // Metodo privato per caricare i clienti disponibili per l'assegnazione.
        clientiMap.clear(); // Pulisce la mappa dei clienti.
        List<String> clienti = new ArrayList<>(); // Crea una nuova lista per i nomi dei clienti.
        String sql = "SELECT u.id, u.nome, u.cognome " + // Query SQL per selezionare ID, nome e cognome degli utenti.
                "FROM Utente u " +
                "JOIN Clienti c ON u.id = c.id_cliente " +
                "WHERE c.id_nutrizionista = ? " +
                "AND u.id NOT IN (SELECT id_cliente FROM Diete WHERE id_cliente IS NOT NULL)"; // Esclude clienti che hanno già una dieta assegnata: Filtra clienti già assegnati.

        try (Connection conn = SQLiteConnessione.connector(); // Ottiene una connessione.
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepara lo statement.

            Integer idNutrizionista = Session.getUserId(); // Ottieni l'ID del nutrizionista dalla Session.
            if (idNutrizionista == null) { // Se l'ID nutrizionista non è disponibile.
                System.err.println("[ERROR - DietaNutrizionista] ID nutrizionista non disponibile dalla Sessione. Impossibile caricare i clienti."); // Stampa errore.
                return clienti; // Restituisce una lista vuota.
            }
            pstmt.setInt(1, idNutrizionista); // Imposta l'ID del nutrizionista.

            ResultSet rs = pstmt.executeQuery(); // Esegue la query.
            while (rs.next()) { // Itera sui risultati.
                String nomeCompleto = (rs.getString("nome") + " " + rs.getString("cognome")).trim().replaceAll("\\s+", " "); // Concatena e formatta il nome completo.
                int idCliente = rs.getInt("id"); // Ottiene l'ID del cliente.

                clienti.add(nomeCompleto); // Aggiunge il nome alla lista.
                clientiMap.put(nomeCompleto, idCliente); // Mappa nome e ID del cliente.
            }

        } catch (SQLException e) { // Cattura eccezioni SQL.
            e.printStackTrace(); // Stampa stack trace.
            showAlert(Alert.AlertType.ERROR,"Errore DB", "Errore durante il caricamento dei clienti disponibili: " + e.getMessage()); // Mostra errore.
        }
        return clienti; // Restituisce la lista dei clienti.
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) { // Metodo privato per mostrare un Alert all'utente.
        Alert alert = new Alert(alertType); // Crea una nuova istanza di Alert con il tipo specificato.
        alert.setTitle(title); // Imposta il titolo dell'alert.
        alert.setHeaderText(null); // Imposta l'header text a null.
        alert.setContentText(message); // Imposta il messaggio di contenuto dell'alert.
        URL cssUrl = getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css"); // Ottiene l'URL del file CSS per lo stile dell'alert.
        if (cssUrl != null) { // Se l'URL del CSS non è nullo.
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm()); // Aggiunge il CSS al dialog pane dell'alert.
            alert.getDialogPane().getStyleClass().add("dialog-pane"); // Apply the base style class: Applica la classe di stile base "dialog-pane".
            // Add specific style class based on AlertType for custom styling
            if (alertType == Alert.AlertType.INFORMATION) { // Se il tipo di alert è INFORMATION.
                alert.getDialogPane().getStyleClass().add("alert-information"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.WARNING) { // Se il tipo di alert è WARNING.
                alert.getDialogPane().getStyleClass().add("alert-warning"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.ERROR) { // Se il tipo di alert è ERROR.
                alert.getDialogPane().getStyleClass().add("alert-error"); // Aggiunge la classe di stile specifica.
            } else if (alertType == Alert.AlertType.CONFIRMATION) { // Se il tipo di alert è CONFIRMATION.
                alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge la classe di stile specifica.
            }
        } else { // Se l'URL del CSS è nullo (file non trovato).
            System.err.println("CSS file not found: Alert-Dialog-Style.css"); // Corrected error message: Stampa un messaggio di errore sulla console degli errori.
        }

        alert.showAndWait(); // Mostra l'alert e attende che l'utente lo chiuda.
    }



    private void apriFinestraModificaGiornoDieta(Dieta dieta, Window ownerWindow) { // Metodo privato per aprire la finestra di modifica del giorno dieta.
        try { // Inizia un blocco try-catch per IOException.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AggiungiGiornoDieta.fxml")); // Carica l'FXML.
            Parent root = loader.load(); // Carica il root node.

            AggiungiGiornoDieta controller = loader.getController(); // Ottiene il controller.
            controller.impostaDietaDaModificare(dieta); // Imposta la dieta da modificare.
            controller.setTitoloPiano(dieta.getNome()); // Imposta il titolo del piano.
            controller.setNumeroGiorni(dieta.getNumeroGiorni()); // Imposta il numero di giorni.

            Stage stage = new Stage(); // Crea un nuovo Stage.
            stage.setTitle("Modifica Giorno Dieta - " + dieta.getNome()); // Imposta il titolo della finestra.
            stage.setScene(new Scene(root)); // Imposta la scena.
            stage.initOwner(ownerWindow); // Imposta la finestra proprietaria.
            stage.initModality(Modality.APPLICATION_MODAL); // Imposta la modalità modale.
            stage.showAndWait(); // Mostra la finestra e attende che si chiuda.

            // Ricarica le diete dopo che la finestra di modifica si è chiusa
            caricaListaDiete(); // Ricarica le liste delle diete.

        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    private void filtraDiete(String searchText) { // Metodo privato per filtrare le diete in base al testo di ricerca.
        // Ricarica le diete complete prima di filtrare per assicurarsi di avere tutti i dati
        caricaListaDiete(); // Ricarica tutte le diete per avere dati aggiornati prima del filtraggio.

        String lowerCaseFilter = searchText.toLowerCase(); // Converte il testo di ricerca in minuscolo.

        ObservableList<Dieta> filteredAssegnate = observableListaDieteAssegnate.stream() // Crea uno stream dalla lista delle diete assegnate.
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter)) // Filtra le diete il cui nome contiene il testo di ricerca.
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Raccoglie i risultati in una nuova ObservableList.

        ObservableList<Dieta> filteredDaAssegnare = observableListaDieteDaAssegnare.stream() // Crea uno stream dalla lista delle diete da assegnare.
                .filter(dieta -> dieta.getNome().toLowerCase().contains(lowerCaseFilter)) // Filtra le diete il cui nome contiene il testo di ricerca.
                .collect(Collectors.toCollection(FXCollections::observableArrayList)); // Raccoglie i risultati in una nuova ObservableList.

        // Aggiorna le ObservableList con i risultati filtrati
        observableListaDieteAssegnate.setAll(filteredAssegnate); // Aggiorna la lista delle diete assegnate con i risultati filtrati.
        observableListaDieteDaAssegnare.setAll(filteredDaAssegnare); // Aggiorna la lista delle diete da assegnare con i risultati filtrati.
    }

    @FXML
    private void vaiAggiungiNuovaDieta(ActionEvent event) { // Metodo FXML per navigare alla schermata di aggiunta nuova dieta.
        try { // Inizia un blocco try-catch per IOException.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/NuovaDieta.fxml")); // Carica l'FXML di NuovaDieta.
            Parent root = loader.load(); // Carica il root node.
            Stage stage = new Stage(); // Crea un nuovo Stage.
            stage.setTitle("Nuova Dieta"); // Imposta il titolo.
            stage.setScene(new Scene(root)); // Imposta la scena.
            stage.setResizable(false); // Rende non ridimensionabile.
            stage.setFullScreen(false); // Disabilita schermo intero.
            stage.initModality(Modality.APPLICATION_MODAL); // Imposta la modalità modale.
            Window owner = ((Node) event.getSource()).getScene().getWindow(); // Ottiene la finestra proprietaria.
            stage.initOwner(owner); // Imposta il proprietario.

            // Quando la finestra di NuovaDieta si chiude, ricarica le liste
            stage.setOnHidden(e -> caricaListaDiete()); // Aggiunge un gestore per l'evento di chiusura della finestra.

            stage.show(); // Mostra la finestra.
        } catch (IOException e) { // Cattura IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void eliminaDietaSelezionata(ActionEvent event) { // Metodo FXML per eliminare una dieta selezionata.
        Dieta dietaSelezionata = null; // Variabile per la dieta selezionata, inizializzata a null.

        if (listaDieteDaAssegnare.getSelectionModel().getSelectedItem() != null) { // Controlla se una dieta è selezionata nella lista "Diete da Assegnare".
            dietaSelezionata = listaDieteDaAssegnare.getSelectionModel().getSelectedItem(); // Seleziona la dieta dall'elenco "Diete da Assegnare".
        }

        if (dietaSelezionata == null) { // Se nessuna dieta è selezionata (o la selezione è nulla per qualche motivo).
            showAlert(Alert.AlertType.ERROR,"Attenzione", "Seleziona una dieta non assegnata da eliminare."); // Mostra un messaggio di errore all'utente.
            return; // Termina l'esecuzione del metodo.
        }


        boolean conferma = confermaEliminazione(dietaSelezionata.getNome()); // Chiede all'utente una conferma per l'eliminazione della dieta.
        if (!conferma) {
            return; // L'utente ha annullato: Termina l'esecuzione del metodo.
        }


        try (Connection conn = SQLiteConnessione.connector()) { // Tenta di ottenere una connessione al database utilizzando la classe SQLiteConnessione.
            conn.setAutoCommit(false); // Disabilita l'autocommit per la connessione; questo permette di gestire la transazione manualmente.

            try { // Inizia un blocco try-catch interno per gestire le eccezioni durante le operazioni del database all'interno della transazione.
                // 1. Recupera gli ID dei giorni di dieta associati a questa dieta
                String queryGiorni = "SELECT id_giorno_dieta FROM Giorno_dieta WHERE id_dieta = ?"; // Query SQL per selezionare gli ID dei giorni di dieta legati all'ID della dieta selezionata.
                PreparedStatement psGiorni = conn.prepareStatement(queryGiorni); // Prepara lo statement SQL con la query definita.
                psGiorni.setInt(1, dietaSelezionata.getId()); // Imposta il parametro (l'ID della dieta selezionata) nella query.
                ResultSet rs = psGiorni.executeQuery(); // Esegue la query e ottiene il set di risultati (gli ID dei giorni).

                java.util.List<Integer> listaIdGiorni = new java.util.ArrayList<>(); // Crea una nuova lista per memorizzare gli ID dei giorni di dieta recuperati.
                while (rs.next()) { // Itera su ogni riga del ResultSet.
                    listaIdGiorni.add(rs.getInt("id_giorno_dieta")); // Aggiunge l'ID del giorno di dieta corrente alla lista.
                }
                rs.close(); // Chiude il ResultSet per liberare le risorse.
                psGiorni.close(); // Chiude il PreparedStatement per liberare le risorse.

                // 2. Elimina gli alimenti associati a questi giorni di dieta (se ce ne sono)
                if (!listaIdGiorni.isEmpty()) { // Controlla se la lista degli ID dei giorni non è vuota (cioè, ci sono giorni di dieta da considerare).
                    StringBuilder sb = new StringBuilder(); // Crea uno StringBuilder per costruire la query SQL dinamicamente.
                    sb.append("DELETE FROM DietaAlimenti WHERE id_giorno_dieta IN ("); // Inizia la query DELETE FROM per la tabella DietaAlimenti.
                    for (int i = 0; i < listaIdGiorni.size(); i++) { // Itera sulla lista degli ID dei giorni.
                        sb.append("?"); // Aggiunge un placeholder '?' per ogni ID di giorno.
                        if (i < listaIdGiorni.size() - 1) { // Se non è l'ultimo ID nella lista.
                            sb.append(","); // Aggiunge una virgola per separare i placeholder.
                        }
                    }
                    sb.append(")"); // Chiude la parentesi della clausola IN.

                    PreparedStatement psEliminaAlimenti = conn.prepareStatement(sb.toString()); // Prepara lo statement SQL con la query DELETE costruita.
                    for (int i = 0; i < listaIdGiorni.size(); i++) { // Itera nuovamente sulla lista degli ID dei giorni.
                        psEliminaAlimenti.setInt(i + 1, listaIdGiorni.get(i)); // Imposta ogni ID di giorno come parametro nella query (l'indice inizia da 1).
                    }
                    psEliminaAlimenti.executeUpdate(); // Esegue la query di cancellazione degli alimenti associati.
                    psEliminaAlimenti.close(); // Chiude il PreparedStatement.
                }

                // 3. Elimina i giorni di dieta
                String eliminaGiorni = "DELETE FROM Giorno_dieta WHERE id_dieta = ?"; // Query SQL per eliminare i giorni di dieta associati all'ID della dieta.
                try (PreparedStatement ps = conn.prepareStatement(eliminaGiorni)) { // Prepara lo statement per l'eliminazione dei giorni.
                    ps.setInt(1, dietaSelezionata.getId()); // Imposta l'ID della dieta come parametro.
                    ps.executeUpdate(); // Esegue la query di cancellazione.
                } // Il PreparedStatement si chiude automaticamente qui.

                // 4. Elimina la dieta stessa
                String eliminaDieta = "DELETE FROM Diete WHERE id = ?"; // Query SQL per eliminare la dieta principale.
                try (PreparedStatement ps = conn.prepareStatement(eliminaDieta)) { // Prepara lo statement per l'eliminazione della dieta.
                    ps.setInt(1, dietaSelezionata.getId()); // Imposta l'ID della dieta come parametro.
                    ps.executeUpdate(); // Esegue la query di cancellazione.
                } // Il PreparedStatement si chiude automaticamente qui.

                conn.commit(); // Conferma la transazione: se tutte le operazioni precedenti sono andate a buon fine, le modifiche vengono salvate definitivamente nel database.

                // Rimuovi la dieta dalla ObservableList corrispondente nell'UI

                observableListaDieteDaAssegnare.remove(dietaSelezionata); // Rimuove l'oggetto Dieta dall'ObservableList che alimenta la ListView "Diete da Assegnare", aggiornando l'interfaccia utente.

                showAlert(Alert.AlertType.INFORMATION,"Successo", "Dieta eliminata correttamente."); // Mostra un messaggio di successo all'utente.

            } catch (SQLException e) { // Cattura le eccezioni SQL che possono verificarsi all'interno del blocco transazionale.
                conn.rollback(); // Esegui il rollback in caso di errore: In caso di errore, esegue il rollback della transazione, annullando tutte le modifiche fatte nel blocco try.
                System.err.println("Errore durante l'eliminazione della dieta (rollback): " + e.getMessage()); // Stampa un messaggio di errore dettagliato sulla console.
                showAlert(Alert.AlertType.ERROR,"Errore Eliminazione", "Si è verificato un errore durante l'eliminazione della dieta: " + e.getMessage()); // Mostra un messaggio di errore all'utente tramite un Alert.
            }

        } catch (SQLException e) { // Cattura le eccezioni SQL relative alla connessione al database (esterne al blocco transazionale).
            System.err.println("Errore di connessione DB: " + e.getMessage()); // Stampa un messaggio di errore sulla connessione al database.
            showAlert(Alert.AlertType.ERROR,"Errore Connessione", "Impossibile connettersi al database: " + e.getMessage()); // Mostra un messaggio di errore all'utente sulla connessione al database.
        }
    }

    private boolean confermaEliminazione(String nomeDieta) { // Metodo privato per chiedere conferma all'utente prima di eliminare una dieta.
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION); // Crea un nuovo Alert di tipo CONFIRMATION.
        alert.setTitle("Conferma Eliminazione"); // Imposta il titolo dell'alert.
        alert.setHeaderText("Sei sicuro di voler eliminare la dieta \"" + nomeDieta + "\"?"); // Imposta l'header text dell'alert, includendo il nome della dieta.
        alert.setContentText("La dieta non potrà essere recuperata."); // Imposta il messaggio di contenuto dell'alert, avvisando che l'azione è irreversibile.
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/matteotocci/app/css/Alert-Dialog-Style.css").toExternalForm()); // Carica e aggiunge un foglio di stile CSS personalizzato per l'alert.
        alert.getDialogPane().getStyleClass().add("dialog-pane"); // Aggiunge la classe di stile "dialog-pane" al pannello del dialogo.
        alert.getDialogPane().getStyleClass().add("alert-confirmation"); // Aggiunge una classe di stile specifica per gli alert di conferma.


        Optional<ButtonType> result = alert.showAndWait(); // Mostra l'alert e attende che l'utente interagisca (clicchi su un bottone). Il risultato è un Optional di ButtonType.
        return result.isPresent() && result.get() == ButtonType.OK; // Restituisce true se l'utente ha cliccato sul bottone "OK", altrimenti false.
    }

    @FXML
    private void vaiAiClienti(ActionEvent event) { // Metodo FXML per navigare alla pagina "HomePageNutrizionista" (presumibilmente la pagina dei clienti).
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/HomePageNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML della HomePage del Nutrizionista.
            Parent homePageRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage (la finestra) corrente dal Node che ha generato l'evento.
            stage.setScene(new Scene(homePageRoot)); // Imposta la nuova scena sullo Stage, visualizzando la HomePage.
            stage.show(); // Mostra lo Stage aggiornato.
        } catch (IOException e) { // Cattura l'eccezione IOException se il caricamento del file FXML fallisce.
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione per debug.
        }
    }

    @FXML
    private void openProfiloNutrizionista(MouseEvent event) { // Metodo FXML per aprire la pagina del profilo del Nutrizionista.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/ProfiloNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML del Profilo Nutrizionista.
            Parent profileRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage profileStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            profileStage.setScene(new Scene(profileRoot)); // Imposta la nuova scena con il profilo.
            profileStage.show(); // Mostra lo Stage aggiornato.
        } catch (IOException e) { // Cattura l'eccezione IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void AccessoAlimenti(ActionEvent event) { // Metodo FXML per navigare alla pagina di gestione degli Alimenti.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/AlimentiNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML degli Alimenti del Nutrizionista.
            Parent alimentiRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage alimentiStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            alimentiStage.setScene(new Scene(alimentiRoot)); // Imposta la nuova scena con gli alimenti.
            alimentiStage.show(); // Mostra lo Stage aggiornato.
        } catch (IOException e) { // Cattura l'eccezione IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }

    @FXML
    private void AccessoRicette(ActionEvent event) { // Metodo FXML per navigare alla pagina di gestione delle Ricette.
        try { // Inizia un blocco try-catch per gestire l'IOException.
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/matteotocci/app/RicetteNutrizionista.fxml")); // Crea un FXMLLoader per caricare il file FXML delle Ricette del Nutrizionista.
            Parent ricetteRoot = fxmlLoader.load(); // Carica la gerarchia di oggetti dal file FXML.
            Stage ricetteStage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // Ottiene lo Stage corrente.
            ricetteStage.setScene(new Scene(ricetteRoot)); // Imposta la nuova scena con le ricette.
            ricetteStage.setTitle("Ricette"); // Imposta il titolo della finestra.
            ricetteStage.show(); // Mostra lo Stage aggiornato.
        } catch (IOException e) { // Cattura l'eccezione IOException.
            e.printStackTrace(); // Stampa lo stack trace.
        }
    }
}
