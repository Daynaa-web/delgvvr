package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showAlert("Анхааруулга", "Бүх талбарыг бөглөнө үү!", Alert.AlertType.WARNING);
            return;
        }
        if (username.length() < 3) {
            showAlert("Анхааруулга", "Нэвтрэх нэр хамгийн багадаа 3 тэмдэгт байна!", Alert.AlertType.WARNING);
            return;
        }
        if (password.length() < 4) {
            showAlert("Анхааруулга", "Нууц үг хамгийн багадаа 4 тэмдэгт байна!", Alert.AlertType.WARNING);
            return;
        }
        if (!password.equals(confirm)) {
            showAlert("Алдаа", "Нууц үг зөрүүтэй байна!", Alert.AlertType.ERROR);
            confirmField.clear();
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
                check.setString(1, username);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        showAlert("Алдаа", "Энэ нэвтрэх нэр бүртгэлтэй байна!", Alert.AlertType.ERROR);
                        return;
                    }
                }
            }

            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, 'CASHIER')")) {
                ins.setString(1, username);
                ins.setString(2, password);
                ins.executeUpdate();
            }

            showAlert("Амжилттай", "Бүртгэл үүслээ. Одоо нэвтэрч орно уу.", Alert.AlertType.INFORMATION);
            Main.showLogin();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Алдаа", "Өгөгдлийн сангийн алдаа: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Алдаа", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            Main.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
