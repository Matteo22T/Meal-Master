
package com.matteotocci.app.model;

public class Dieta {
    private int id;
    private String nome;
    private String dataInizio;
    private String dataFine;
    private int idCliente; // Aggiungi questo campo
    private int numeroGiorni; // Aggiungi questo campo

    public Dieta(int id, String nome, String dataInizio, String dataFine) {
        this.id = id;
        this.nome = nome;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idCliente = 0; // Default a non assegnato
        this.numeroGiorni = 0; // Default
    }

    // Costruttore che include idCliente (opzionale, puoi impostarlo con il setter)
    public Dieta(int id, String nome, String dataInizio, String dataFine, int idCliente, int numeroGiorni) {
        this.id = id;
        this.nome = nome;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.idCliente = idCliente;
        this.numeroGiorni = numeroGiorni;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDataInizio() {
        return dataInizio;
    }

    public String getDataFine() {
        return dataFine;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getNumeroGiorni() {
        return numeroGiorni;
    }

    public void setNumeroGiorni(int numeroGiorni) {
        this.numeroGiorni = numeroGiorni;
    }

    @Override
    public String toString() {
        return nome + " (Inizio: " + dataInizio + ", Fine: " + dataFine + ")";
    }
}