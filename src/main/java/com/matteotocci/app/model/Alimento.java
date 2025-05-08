package com.matteotocci.app.model;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Alimento {
    private final ImageView immagine;
    private String imgUrl;
    private String nome,brand;
    private double kcal, proteine, carboidrati, grassi, grassiSaturi, sale, fibre, zuccheri;
    private Integer userId, id;  // Modifica l'ID utente come Integer



    public Alimento(String nome, String brand, double kcal, double proteine, double carboidrati, double grassi,
                    double grassiSaturi, double sale, double fibre, double zuccheri, String imgPath, String imgUrl, Integer userId, Integer id) {
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
        this.userId = userId;
        this.id = id;


        this.imgUrl=imgUrl;

        Image img = null;
        if (imgPath != null && !imgPath.isBlank()) {
            img = new Image(imgPath, 50, 50, true, true, true);
        } else {
            img = new Image("com/matteotocci/app/immagini/png-clipart-computer-icons-encapsulated-postscript-dish-dish-love-food-thumbnail.png", 50, 50, true, true, true); // se vuoi una fallback image
        }
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
    public Integer getUserId() { return userId; }
    public Integer getId() { return id; }

    public String getImmagineGrande() {
        return imgUrl;
    }
}
