package com.nexusbank.security;

import com.nexusbank.core.DatabaseManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BiometricAuth - simple simulated biometric enrollment and verification
 * Stores SHA-256 hash of a user-provided "pattern" string in users.biometric_hash
 */
public class BiometricAuth {

    private DatabaseManager db;

    public BiometricAuth() {
        this.db = DatabaseManager.getInstance();
    }

    /**
     * Enroll biometric pattern for user
     */
    public boolean enroll(int userId, String pattern) throws SQLException {
        String hash = sha256(pattern);
        String q = "UPDATE users SET biometric_hash=? WHERE user_id=?";
        PreparedStatement ps = db.prepareStatement(q);
        ps.setString(1, hash);
        ps.setInt(2, userId);
        int updated = ps.executeUpdate();
        ps.close();
        return updated > 0;
    }

    /**
     * Verify biometric pattern
     */
    public boolean verify(int userId, String pattern) throws SQLException {
        String q = "SELECT biometric_hash FROM users WHERE user_id=?";
        PreparedStatement ps = db.prepareStatement(q);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        String stored = null;
        if (rs.next()) stored = rs.getString("biometric_hash");
        rs.close();
        ps.close();
        if (stored == null) return false;
        String hash = sha256(pattern);
        return stored.equals(hash);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
