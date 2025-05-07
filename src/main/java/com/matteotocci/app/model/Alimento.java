package com.matteotocci.app.model;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Alimento {
    private final ImageView immagine;
    private String imgUrl;
    private String nome,brand;
    private double kcal, proteine, carboidrati, grassi, grassiSaturi, sale, fibre, zuccheri;

    public Alimento(String nome, String brand, double kcal, double proteine, double carboidrati, double grassi,
                    double grassiSaturi, double sale, double fibre, double zuccheri, String imgPath, String imgUrl) {
        this.nome = nome;
        this.brand = brand;
        this.kcal = kcal;
        this.proteine = proteine;
        this.carboidrati = carboidrati;
        this.grassi = grassi;
        this.grassiSaturi = grassiSaturi;
        this.sale = sale;
        this.fibre = fibre;
        this.zuccheri = zuccheri;

        this.imgUrl=imgUrl;

        Image img = new Image(imgPath, 50, 50, true, true, true); // ultimo true = background loading
        this.immagine = new ImageView(img);
    }

    public ImageView getImmagine() { return immagine; }
    public String getNome() { return nome; }
    public double getKcal() { return kcal; }
    public double getProteine() { return proteine; }
    public double getCarboidrati() { return carboidrati; }
    public double getGrassi() { return grassi; }
    public double getGrassiSaturi() { return grassiSaturi; }
    public double getSale() { return sale; }
    public double getFibre() { return fibre; }
    public double getZuccheri() { return zuccheri; }
    public String getBrand() { return brand; }

    public String getImmagineGrande() {
        return imgUrl;
    }
}
