package com.matteotocci.app.model;

public class Pasto {
    private int idPasto;
    private int idPastiGiornaliero;
    private String tipo; // "alimento" or "ricetta"
    private int idElemento; // id_alimento or id_ricetta
    private double quantitaGrammi;

    public Pasto(int idPasto, int idPastiGiornaliero, String tipo, int idElemento, double quantitaGrammi) {
        this.idPasto = idPasto;
        this.idPastiGiornaliero = idPastiGiornaliero;
        this.tipo = tipo;
        this.idElemento = idElemento;
        this.quantitaGrammi = quantitaGrammi;
    }

    // Getters
    public int getIdPasto() {
        return idPasto;
    }

    public int getIdPastiGiornaliero() {
        return idPastiGiornaliero;
    }

    public String getTipo() {
        return tipo;
    }

    public int getIdElemento() {
        return idElemento;
    }

    public double getQuantitaGrammi() {
        return quantitaGrammi;
    }
}
