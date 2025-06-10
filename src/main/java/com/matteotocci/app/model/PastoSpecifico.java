package com.matteotocci.app.model;

import javafx.beans.property.*;

public class PastoSpecifico {
    private final IntegerProperty pastoId; // NUOVO: Proprietà per memorizzare l'ID del Pasto dal database
    private final StringProperty name;
    private final DoubleProperty quantity;
    private final DoubleProperty kcal;
    private final DoubleProperty protein;
    private final DoubleProperty carb;
    private final DoubleProperty fat;

    public PastoSpecifico(String name, double quantity, double kcal, double protein, double carb, double fat) {
        this.pastoId = new SimpleIntegerProperty(0); // Inizializza con un valore di default.
        // Verrà impostato correttamente dopo aver letto dal DB.
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.kcal = new SimpleDoubleProperty(kcal);
        this.protein = new SimpleDoubleProperty(protein);
        this.carb = new SimpleDoubleProperty(carb);
        this.fat = new SimpleDoubleProperty(fat);
    }

    // --- Metodi Getter e Setter per pastoId ---
    public int getPastoId() {
        return pastoId.get();
    }

    public void setPastoId(int pastoId) {
        this.pastoId.set(pastoId);
    }

    public IntegerProperty pastoIdProperty() {
        return pastoId;
    }

    // --- Metodi Getter per le altre proprietà (già presenti prima) ---
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getQuantity() {
        return quantity.get();
    }

    public DoubleProperty quantityProperty() {
        return quantity;
    }

    public double getKcal() {
        return kcal.get();
    }

    public DoubleProperty kcalProperty() {
        return kcal;
    }

    public double getProtein() {
        return protein.get();
    }

    public DoubleProperty proteinProperty() {
        return protein;
    }

    public double getCarb() {
        return carb.get();
    }

    public DoubleProperty carbProperty() {
        return carb;
    }

    public double getFat() {
        return fat.get();
    }

    public DoubleProperty fatProperty() {
        return fat;
    }

    public void setQuantity(double newQuantity) {
        quantity.set(newQuantity);
    }
    public void setKcal(double newKcal) {
        kcal.set(newKcal);
    }
    public void setProtein(double newProtein) {
        protein.set(newProtein);
    }
    public void setCarb(double newCarb) {
        carb.set(newCarb);
    }
    public void setFat(double newfat) {
        fat.set(newfat);
    }


}
