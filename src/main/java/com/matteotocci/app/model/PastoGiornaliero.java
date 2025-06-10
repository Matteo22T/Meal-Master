package com.matteotocci.app.model;

import java.time.LocalDate;

public class PastoGiornaliero {
    private int idPastiGiornaliero;
    private int idCliente;
    private int idGiornoDieta;
    private LocalDate data;
    private String pasto; // e.g., "Colazione", "Pranzo"
    private double kcal;
    private double proteine;
    private double carboidrati;
    private double grassi;

    public PastoGiornaliero(int idPastiGiornaliero, int idCliente, int idGiornoDieta, LocalDate data, String pasto, double kcal, double proteine, double carboidrati, double grassi) {
        this.idPastiGiornaliero = idPastiGiornaliero;
        this.idCliente = idCliente;
        this.idGiornoDieta = idGiornoDieta;
        this.data = data;
        this.pasto = pasto;
        this.kcal = kcal;
        this.proteine = proteine;
        this.carboidrati = carboidrati;
        this.grassi = grassi;
    }

    // Getters
    public int getIdPastiGiornaliero() {
        return idPastiGiornaliero;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public int getIdGiornoDieta() {
        return idGiornoDieta;
    }

    public LocalDate getData() {
        return data;
    }

    public String getPasto() {
        return pasto;
    }

    public double getKcal() {
        return kcal;
    }

    public double getProteine() {
        return proteine;
    }

    public double getCarboidrati() {
        return carboidrati;
    }

    public double getGrassi() {
        return grassi;
    }

    // Setters (if needed)
}