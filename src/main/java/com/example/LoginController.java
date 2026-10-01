package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Анхааруулга", "Бүх талбарыг бөглөнө үү!", Alert.AlertType.WARNING);
            return;
        }

        String query = "SELECT id, username, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Session.login(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                    Main.showSales();
                } else {
                    showAlert("Алдаа", "Нэвтрэх нэр эсвэл нууц үг буруу байна!", Alert.AlertType.ERROR);
                    passwordField.clear();
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            showAlert("Алдаа", "Өгөгдлийн сантай холбогдож чадсангүй.\nMySQL асаалттай эсэх, pos_system сан үүссэн эсэхийг шалгана уу.\n\n" + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Алдаа", "Цонх нээхэд алдаа гарлаа: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGoRegister() {
        try {
            Main.showRegister();
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
