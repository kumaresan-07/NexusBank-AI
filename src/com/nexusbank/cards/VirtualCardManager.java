package com.nexusbank.cards;

import com.nexusbank.core.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * VirtualCardManager - Handles virtual card creation, management, and transactions
 */
public class VirtualCardManager {
    
    private int userId;
    private DatabaseManager db;
    
    public VirtualCardManager(int userId) {
        this.userId = userId;
        this.db = DatabaseManager.getInstance();
    }
    
    /**
     * Create a new virtual card
     */
    public Map<String, Object> createVirtualCard(String cardNickname, String cardType, double spendingLimit, boolean isTemporary, int validityDays) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Generate card details
            String cardNumber = generateCardNumber();
            String cardPin = generatePIN();
            String status = "ACTIVE";
            
            // Calculate expiry timestamp
            Timestamp expiresAt = null;
            if (isTemporary) {
                LocalDateTime expiry = LocalDateTime.now().plusDays(validityDays);
                expiresAt = Timestamp.valueOf(expiry);
            }
            
            String sql = "INSERT INTO virtual_cards (user_id, card_number, card_pin, card_nickname, " +
                        "spending_limit, expires_at, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.setString(2, cardNumber);
            pstmt.setString(3, cardPin);
            pstmt.setString(4, cardNickname);
            pstmt.setDouble(5, spendingLimit);
            pstmt.setTimestamp(6, expiresAt);
            pstmt.setString(7, status);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int cardId = rs.getInt(1);
                    
                    result.put("success", true);
                    result.put("cardId", cardId);
                    result.put("cardNumber", maskCardNumber(cardNumber));
                    result.put("cardPin", cardPin);
                    result.put("expiresAt", expiresAt);
                    result.put("message", "Virtual card created successfully!");
                    
                    System.out.println("Virtual card created: " + cardNickname + " for user " + userId);
                }
                rs.close();
            }
            pstmt.close();
            
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Error creating virtual card: " + e.getMessage());
            System.err.println("Error creating virtual card: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get all virtual cards for the user
     */
    public List<Map<String, Object>> getUserCards() {
        List<Map<String, Object>> cards = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM virtual_cards WHERE user_id = ? ORDER BY created_at DESC";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> card = new HashMap<>();
                card.put("cardId", rs.getInt("card_id"));
                card.put("cardNickname", rs.getString("card_nickname"));
                card.put("cardNumber", maskCardNumber(rs.getString("card_number")));
                card.put("cardPin", rs.getString("card_pin"));
                card.put("spendingLimit", rs.getDouble("spending_limit"));
                card.put("usageCount", rs.getInt("usage_count"));
                card.put("status", rs.getString("status"));
                card.put("createdAt", rs.getTimestamp("created_at"));
                card.put("expiresAt", rs.getTimestamp("expires_at"));
                card.put("isActive", "ACTIVE".equals(rs.getString("status")));
                
                cards.add(card);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error fetching user cards: " + e.getMessage());
        }
        
        return cards;
    }
    
    /**
     * Process a transaction with virtual card
     */
    public Map<String, Object> processTransaction(int cardId, double amount, String merchant, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First check if card exists and is valid
            String checkSql = "SELECT * FROM virtual_cards WHERE card_id = ? AND user_id = ? AND status = 'ACTIVE'";
            PreparedStatement checkStmt = db.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, cardId);
            checkStmt.setInt(2, userId);
            
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                result.put("success", false);
                result.put("message", "Card not found or inactive");
                rs.close();
                checkStmt.close();
                return result;
            }
            
            double spendingLimit = rs.getDouble("spending_limit");
            int usageCount = rs.getInt("usage_count");
            String cardNickname = rs.getString("card_nickname");
            Timestamp expiresAt = rs.getTimestamp("expires_at");
            
            // Check if card is expired
            if (expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
                result.put("success", false);
                result.put("message", "Card has expired");
                rs.close();
                checkStmt.close();
                return result;
            }
            
            // Simple spending limit check (could be enhanced with daily/monthly limits)
            if (amount > spendingLimit) {
                result.put("success", false);
                result.put("message", "Transaction exceeds spending limit");
                rs.close();
                checkStmt.close();
                return result;
            }
            
            rs.close();
            checkStmt.close();
            
            // Process the transaction
            db.getConnection().setAutoCommit(false);
            
            try {
                // Update card usage count
                String updateCardSql = "UPDATE virtual_cards SET usage_count = usage_count + 1 WHERE card_id = ?";
                PreparedStatement updateStmt = db.getConnection().prepareStatement(updateCardSql);
                updateStmt.setInt(1, cardId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                // Record transaction in main transactions table
                String transSql = "INSERT INTO transactions (user_id, transaction_type, amount, description, timestamp) " +
                                 "VALUES (?, 'PURCHASE', ?, ?, ?)";
                PreparedStatement transStmt = db.getConnection().prepareStatement(transSql);
                transStmt.setInt(1, userId);
                transStmt.setDouble(2, -amount); // Negative for purchase
                transStmt.setString(3, "Virtual Card: " + cardNickname + " - " + description + " at " + merchant);
                transStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                transStmt.executeUpdate();
                transStmt.close();
                
                db.getConnection().commit();
                
                result.put("success", true);
                result.put("message", "Transaction successful");
                result.put("amount", amount);
                result.put("merchant", merchant);
                result.put("usageCount", usageCount + 1);
                
                System.out.println("Virtual card transaction processed: " + amount + " at " + merchant);
                
            } catch (SQLException e) {
                db.getConnection().rollback();
                throw e;
            } finally {
                db.getConnection().setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Transaction failed: " + e.getMessage());
            System.err.println("Transaction error: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Deactivate a virtual card
     */
    public boolean deactivateCard(int cardId) {
        try {
            String sql = "UPDATE virtual_cards SET status = 'INACTIVE' WHERE card_id = ? AND user_id = ?";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setInt(1, cardId);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            if (rowsAffected > 0) {
                System.out.println("Virtual card " + cardId + " deactivated");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deactivating card: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update card spending limit
     */
    public boolean updateSpendingLimit(int cardId, double newLimit) {
        try {
            String sql = "UPDATE virtual_cards SET spending_limit = ? WHERE card_id = ? AND user_id = ?";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setDouble(1, newLimit);
            pstmt.setInt(2, cardId);
            pstmt.setInt(3, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating spending limit: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    private String generateCardNumber() {
        // Generate a 16-digit card number (starts with 4000 for virtual cards)
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder("4000");
        
        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        return cardNumber.toString();
    }
    
    private String generatePIN() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() != 16) return cardNumber;
        return cardNumber.substring(0, 4) + " **** **** " + cardNumber.substring(12);
    }
}