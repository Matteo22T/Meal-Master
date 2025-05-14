package com.matteotocci.app.model;

public class Ricetta {
    private int id;
    private String nome;
    private String descrizione;
    private String categoria;
    private int userId;

    public Ricetta(int id, String nome, String descrizione, String categoria, int userId) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.categoria = categoria;
        this.userId = userId;
    }

    // Getter e Setter
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    public String getCategoria() { return categoria; }
    public int getUserId() { return userId; }
}
