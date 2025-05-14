package com.matteotocci.app.model;

import com.matteotocci.app.controller.Alimenti;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class IngredienteRicetta {
    private Alimento alimento;
    private DoubleProperty quantita;

    public IngredienteRicetta(Alimento alimento, double quantita) {
        this.alimento = alimento;
        this.quantita = new SimpleDoubleProperty(quantita);
    }

    public Alimento getAlimento() { return alimento; }
    public double getQuantita() { return quantita.get(); }
    public void setQuantita(double q) { quantita.set(q); }
    public DoubleProperty quantitaProperty() { return quantita; }
}
