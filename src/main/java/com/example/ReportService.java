package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportService {

    // 1. Өдрийн борлуулалтын нийт дүн болон тоог харах
    public static void getDailyReport(String date) { // Жишээ формат: '2026-06-07'
        String sql = "SELECT COUNT(*) as total_sales, SUM(total_amount) as total_revenue FROM sales WHERE DATE(sale_date) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("total_sales");
                double revenue = rs.getDouble("total_revenue");
                System.out.println("Өдрийн борлуулалтын тоо: " + count);
                System.out.println("Өдрийн нийт орлого: " + revenue + " ₮");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Хамгийн их зарагдсан барааны жагсаалт гаргах
    public static void getTopSellingProducts() {
        String sql = "SELECT p.name, SUM(si.quantity) as total_qty " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.id " +
                     "GROUP BY p.id, p.name " +
                     "ORDER BY total_qty DESC LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("--- Хамгийн их зарагдсан топ 5 бараа ---");
            while (rs.next()) {
                String name = rs.getString("name");
                int qty = rs.getInt("total_qty");
                System.out.println("Бараа: " + name + " | Зарагдсан тоо: " + qty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}