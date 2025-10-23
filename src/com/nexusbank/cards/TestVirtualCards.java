package com.nexusbank.cards;

import com.nexusbank.core.DatabaseManager;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * TestVirtualCards - Utility to test the Virtual Cards functionality
 */
public class TestVirtualCards {

    public static void main(String[] args) {
        try {
            System.out.println("Testing Virtual Cards functionality...");
            
            // Test VirtualCardManager
            VirtualCardManager cardManager = new VirtualCardManager(1);
            
            System.out.println("\n=== Creating Test Virtual Cards ===");
            
            // Create a shopping card
            System.out.println("Creating shopping card...");
            Map<String, Object> result1 = cardManager.createVirtualCard("Shopping Card", "Shopping", 5000.0, false, 365);
            System.out.println("Shopping card result: " + result1.get("message"));
            
            // Create a temporary subscription card
            System.out.println("Creating subscription card...");
            Map<String, Object> result2 = cardManager.createVirtualCard("Netflix Subscription", "Subscription", 1000.0, true, 30);
            System.out.println("Subscription card result: " + result2.get("message"));
            
            // Create a travel card
            System.out.println("Creating travel card...");
            Map<String, Object> result3 = cardManager.createVirtualCard("Travel Expenses", "Travel", 10000.0, false, 180);
            System.out.println("Travel card result: " + result3.get("message"));
            
            System.out.println("\n=== Retrieving User Cards ===");
            List<Map<String, Object>> cards = cardManager.getUserCards();
            System.out.println("Total cards found: " + cards.size());
            
            for (Map<String, Object> card : cards) {
                System.out.println("Card: " + card.get("cardNickname") + 
                                 " | Number: " + card.get("cardNumber") + 
                                 " | Limit: ₹" + card.get("spendingLimit") +
                                 " | Status: " + card.get("status") +
                                 " | Usage: " + card.get("usageCount"));
            }
            
            // Test transaction if cards exist
            if (!cards.isEmpty()) {
                System.out.println("\n=== Testing Transaction ===");
                Map<String, Object> firstCard = cards.get(0);
                int cardId = (Integer) firstCard.get("cardId");
                
                System.out.println("Processing transaction on card: " + firstCard.get("cardNickname"));
                Map<String, Object> transResult = cardManager.processTransaction(cardId, 500.0, "Amazon", "Online Shopping");
                System.out.println("Transaction result: " + transResult.get("message"));
                
                if ((Boolean) transResult.get("success")) {
                    System.out.println("Usage count: " + transResult.get("usageCount"));
                }
            }
            
            System.out.println("\n=== Launching Virtual Cards UI ===");
            
            // Launch the UI
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception e) {
                    System.out.println("Could not set look and feel: " + e.getMessage());
                }
                
                // Import VirtualCardsPanel from ui package
                try {
                    Class<?> panelClass = Class.forName("com.nexusbank.ui.VirtualCardsPanel");
                    Object panel = panelClass.getConstructor(int.class).newInstance(1);
                    panelClass.getMethod("setVisible", boolean.class).invoke(panel, true);
                    
                    // Show instructions
                    JOptionPane.showMessageDialog(
                        null,
                        "Virtual Cards Test Instructions:\n\n" +
                        "1. View existing cards in the table\n" +
                        "2. Click 'Create New Card' to add cards\n" +
                        "3. Select a card and click 'Make Transaction'\n" +
                        "4. Try deactivating a card\n" +
                        "5. Use Refresh to update the display\n" +
                        "6. Check spending limits and remaining balances",
                        "Virtual Cards Test",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                } catch (Exception e) {
                    System.err.println("Could not launch UI: " + e.getMessage());
                    // Fallback - just show the test completed message
                    JOptionPane.showMessageDialog(
                        null,
                        "Virtual Cards backend test completed!\nCheck console for results.",
                        "Test Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error during Virtual Cards test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}