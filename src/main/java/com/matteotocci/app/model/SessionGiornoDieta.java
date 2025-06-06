package com.matteotocci.app.model;

public class SessionGiornoDieta {
    private static GiornoDieta giornoDietaSelezionato;

    public static GiornoDieta getGiornoDietaSelezionato() {
        return giornoDietaSelezionato;
    }

    public static void setGiornoDietaSelezionato(GiornoDieta giorno) {
        giornoDietaSelezionato = giorno;

    }

}
