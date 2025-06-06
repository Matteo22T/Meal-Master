package com.matteotocci.app.model; // Nuovo package per la classe model

public class GiornoDieta {
    private int idGiornoDieta;
    private String nomeGiorno;
    private double kcal;
    private double proteine;
    private double carboidrati;
    private double grassi;

    public GiornoDieta(int idGiornoDieta, String nomeGiorno, double kcal, double proteine, double carboidrati, double grassi) {
        this.idGiornoDieta = idGiornoDieta;
        this.nomeGiorno = nomeGiorno;
        this.kcal = kcal;
        this.proteine = proteine;
        this.carboidrati = carboidrati;
        this.grassi = grassi;
    }

    // Getters
    public int getIdGiornoDieta() { return idGiornoDieta; }
    public String getNomeGiorno() { return nomeGiorno; }
    public double getKcal() { return kcal; }
    public double getProteine() { return proteine; }
    public double getCarboidrati() { return carboidrati; }
    public double getGrassi() { return grassi; }

    @Override
    public String toString() {
        // Questo è il testo che verrà visualizzato nella ComboBox
        return nomeGiorno;
    }
}