package com.matteotocci.app.model;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Apertura extends Application {
    @Override
    public void start(Stage stage)throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(Apertura.class.getResource("/com/matteotocci/app/Alimenti.fxml"));
        Scene scene=new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args){launch();}
}
