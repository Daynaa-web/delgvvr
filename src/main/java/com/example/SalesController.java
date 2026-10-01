package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class SalesController {
    @FXML private TextField productCodeField;
    @FXML private TextField quantityField;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, Double> colPrice;
    @FXML private TableColumn<CartItem, Integer> colQuantity;
    @FXML private TableColumn<CartItem, Double> colSubtotal;
    @FXML private Label totalAmountLabel;
    @FXML private Label userLabel;

    private final ObservableList<CartItem> cartList = FXCollections.observableArrayList();
    private double grandTotal = 0.0;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        cartTable.setItems(cartList);
        cartTable.setPlaceholder(new Label("Сагс хоосон байна"));

        userLabel.setText(Session.getUsername() + " (" + Session.getRole() + ")");
        quantityField.setText("1");
        calculateGrandTotal();
    }

    // Сагсанд бараа нэмэх
    @FXML
    private void handleAddToCart() {
        String code = productCodeField.getText().trim();
        String qtyText = quantityField.getText().trim();

        if (code.isEmpty() || qtyText.isEmpty()) {
            showAlert("Анхааруулга", "Барааны код болон тоо хэмжээг оруулна уу!", Alert.AlertType.WARNING);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Алдаа", "Тоо хэмжээ буруу байна!", Alert.AlertType.ERROR);
            return;
        }

        String sql = "SELECT id, name, price, stock FROM products WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    showAlert("Алдаа", "Энэ кодоор бараа олдсонгүй!", Alert.AlertType.ERROR);
                    return;
                }

                int productId = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                // Сагсанд байгаа ижил барааны тоог тооцно
                CartItem existing = null;
                for (CartItem item : cartList) {
                    if (item.getProductId() == productId) { existing = item; break; }
                }
                int totalQty = qty + (existing != null ? existing.getQuantity() : 0);

                if (stock < totalQty) {
                    showAlert("Анхааруулга", "Агуулахын үлдэгдэл хүрэлцэхгүй байна! Үлдэгдэл: " + stock, Alert.AlertType.WARNING);
                    return;
                }

                if (existing != null) cartList.remove(existing);
                cartList.add(new CartItem(productId, name, price, totalQty, price * totalQty));

                calculateGrandTotal();
                productCodeField.clear();
                quantityField.setText("1");
                productCodeField.requestFocus();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Алдаа", "Өгөгдлийн сангийн алдаа: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Сонгосон барааг сагснаас хасах
    @FXML
    private void handleRemove() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Анхааруулга", "Хасах барааг жагсаалтаас сонгоно уу!", Alert.AlertType.WARNING);
            return;
        }
        cartList.remove(selected);
        calculateGrandTotal();
    }

    // Сагс цэвэрлэх
    @FXML
    private void handleClear() {
        cartList.clear();
        calculateGrandTotal();
    }

    // Гарах
    @FXML
    private void handleLogout() {
        try {
            Session.logout();
            Main.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateGrandTotal() {
        grandTotal = 0.0;
        for (CartItem item : cartList) {
            grandTotal += item.getSubtotal();
        }
        totalAmountLabel.setText(String.format("Нийт: %,.2f ₮", grandTotal));
    }

    // Борлуулалтыг баталгаажуулж хадгалах
    @FXML
    private void handleCheckout() {
        if (cartList.isEmpty()) {
            showAlert("Анхааруулга", "Сагс хоосон байна!", Alert.AlertType.WARNING);
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String insertSale = "INSERT INTO sales (user_id, total_amount) VALUES (?, ?)";
            int saleId = 0;
            try (PreparedStatement saleStmt = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                saleStmt.setInt(1, Session.getUserId());
                saleStmt.setDouble(2, grandTotal);
                saleStmt.executeUpdate();
                try (ResultSet keys = saleStmt.getGeneratedKeys()) {
                    if (keys.next()) saleId = keys.getInt(1);
                }
            }

            String insertItem = "INSERT INTO sale_items (sale_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
            // stock >= ? нөхцөл нь хоёр кассаас зэрэг зарагдахаас хамгаална
            String updateStock = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

            try (PreparedStatement itemStmt = conn.prepareStatement(insertItem);
                 PreparedStatement stockStmt = conn.prepareStatement(updateStock)) {

                for (CartItem item : cartList) {
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getSubtotal());
                    itemStmt.executeUpdate();

                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProductId());
                    stockStmt.setInt(3, item.getQuantity());
                    if (stockStmt.executeUpdate() == 0) {
                        throw new SQLException("Үлдэгдэл хүрэлцэхгүй: " + item.getProductName());
                    }
                }
            }

            conn.commit();
            showAlert("Амжилттай", String.format("Борлуулалт амжилттай хийгдлээ!\nДүн: %,.2f ₮", grandTotal), Alert.AlertType.INFORMATION);
            cartList.clear();
            calculateGrandTotal();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            showAlert("Алдаа", "Борлуулалт хийх явцад алдаа гарлаа:\n" + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
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
