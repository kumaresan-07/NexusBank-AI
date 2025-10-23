package com.nexusbank.security;

import com.nexusbank.core.DatabaseManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestBiometric {
    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            // Ensure demo user exists
            String card = "8888777766665555";
            String pin = "4321";
            int userId = -1;
            ResultSet rs = db.executeQuery("SELECT user_id FROM users WHERE card_number='" + card + "'");
            if (rs.next()) userId = rs.getInt("user_id");
            else {
                rs.close();
                PreparedStatement ps = db.prepareStatement("INSERT INTO users (card_number, primary_pin, full_name) VALUES (?,?,?)");
                ps.setString(1, card);
                ps.setString(2, pin);
                ps.setString(3, "Demo Bio User");
                ps.executeUpdate();
                ps.close();
                rs = db.executeQuery("SELECT user_id FROM users WHERE card_number='" + card + "'");
                if (rs.next()) userId = rs.getInt("user_id");
            }
            if (rs != null && !rs.isClosed()) rs.close();

            BiometricAuth bio = new BiometricAuth();
            boolean enrolled = bio.enroll(userId, "pattern-12345");
            System.out.println("Enrolled: " + enrolled);
            boolean ok = bio.verify(userId, "pattern-12345");
            System.out.println("Verify (correct): " + ok);
            boolean ok2 = bio.verify(userId, "wrong-pattern");
            System.out.println("Verify (wrong): " + ok2);
            db.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
