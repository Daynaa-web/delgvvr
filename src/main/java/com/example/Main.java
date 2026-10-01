package com.example;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        primaryStage.setTitle("Дэлгүүрийн систем");
        showLogin();
        primaryStage.show();
    }

    public static void showLogin() throws IOException {
        setScene("login.fxml", 420, 360, "Дэлгүүрийн систем - Нэвтрэх");
    }

    public static void showRegister() throws IOException {
        setScene("register.fxml", 420, 460, "Дэлгүүрийн систем - Бүртгүүлэх");
    }

    public static void showSales() throws IOException {
        setScene("sales.fxml", 900, 600, "Дэлгүүрийн систем - Борлуулалт");
    }

    private static void setScene(String fxml, double w, double h, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
        Scene scene = new Scene(loader.load(), w, h);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        stage.setTitle(title);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
