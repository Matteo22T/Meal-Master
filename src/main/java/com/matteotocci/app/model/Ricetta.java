package com.matteotocci.app.model;

public class Ricetta {
    private int id;
    private String nome;
    private String descrizione;
    private String categoria;
    private int userId;
    private double kcal;
    private double proteine;
    private double carboidrati;
    private double grassi;
    private double grassiSaturi;
    private double zuccheri;
    private double fibre;
    private double sale;

    public Ricetta(int id, String nome, String descrizione, String categoria, int userId,
                   double kcal, double proteine, double carboidrati, double grassi,
                   double grassiSaturi, double zuccheri, double fibre, double sale) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.categoria = categoria;
        this.kcal = kcal;
        this.proteine = proteine;
        this.carboidrati = carboidrati;
        this.grassi = grassi;
        this.grassiSaturi = grassiSaturi;
        this.zuccheri = zuccheri;
        this.fibre = fibre;
        this.sale = sale;
        this.userId = userId;
    }


    public int getUserId() { return userId; }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public String getCategoria() { return categoria; }

    // Nuovi metodi getter per i valori nutrizionali
    public double getKcal() { return kcal; }
    public double getProteine() { return proteine; }
    public double getCarboidrati() { return carboidrati; }
    public double getGrassi() { return grassi; }
    public double getGrassiSaturi() { return grassiSaturi; }
    public double getZuccheri() { return zuccheri; }
    public double getFibre() { return fibre; }
    public double getSale() { return sale; }
}
