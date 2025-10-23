package com.nexusbank.ui;

import com.nexusbank.cards.VirtualCardManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * VirtualCardsPanel - UI for managing virtual cards
 */
public class VirtualCardsPanel extends JFrame {

    private int userId;
    private VirtualCardManager cardManager;
    
    private JTable cardsTable;
    private DefaultTableModel tableModel;
    private JButton createBtn, transactionBtn, deactivateBtn, refreshBtn, closeBtn;
    private JLabel totalCardsLabel, activeCardsLabel;

    public VirtualCardsPanel(int userId) {
        this.userId = userId;
        this.cardManager = new VirtualCardManager(userId);
        
        setTitle("NexusBank-AI - Virtual Cards Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // Main content
        JPanel main = createMainPanel();
        add(main, BorderLayout.CENTER);
        
        // Bottom buttons
        JPanel bottom = createBottomPanel();
        add(bottom, BorderLayout.SOUTH);
        
        loadCards();
        addActions();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(59, 130, 246)); // Blue color for cards
        header.setPreferredSize(new Dimension(900, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("💳 Virtual Cards Management");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);
        
        // Stats panel
        JPanel stats = new JPanel(new GridLayout(2, 1));
        stats.setOpaque(false);
        
        totalCardsLabel = new JLabel("Total Cards: 0");
        totalCardsLabel.setForeground(Color.WHITE);
        totalCardsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        activeCardsLabel = new JLabel("Active Cards: 0");
        activeCardsLabel.setForeground(Color.WHITE);
        activeCardsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        stats.add(totalCardsLabel);
        stats.add(activeCardsLabel);
        header.add(stats, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Cards table
        String[] columns = {"Card ID", "Card Name", "Card Number", "PIN", "Spending Limit", "Usage Count", "Status", "Expires"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        cardsTable = new JTable(tableModel);
        cardsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cardsTable.setRowHeight(25);
        cardsTable.getTableHeader().setBackground(new Color(229, 231, 235));
        
        // Set column widths
        cardsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        cardsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        cardsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        cardsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        cardsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        cardsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        cardsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        cardsTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(cardsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Virtual Cards"));
        
        main.add(scrollPane, BorderLayout.CENTER);
        
        return main;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        createBtn = new JButton("Create New Card");
        createBtn.setBackground(new Color(34, 197, 94));
        createBtn.setForeground(Color.WHITE);
        
        transactionBtn = new JButton("Make Transaction");
        transactionBtn.setBackground(new Color(59, 130, 246));
        transactionBtn.setForeground(Color.WHITE);
        
        deactivateBtn = new JButton("Deactivate Card");
        deactivateBtn.setBackground(new Color(239, 68, 68));
        deactivateBtn.setForeground(Color.WHITE);
        
        refreshBtn = new JButton("Refresh");
        closeBtn = new JButton("Close");
        
        panel.add(createBtn);
        panel.add(transactionBtn);
        panel.add(deactivateBtn);
        panel.add(refreshBtn);
        panel.add(closeBtn);
        
        return panel;
    }

    private void loadCards() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        List<Map<String, Object>> cards = cardManager.getUserCards();
        
        int totalCards = cards.size();
        int activeCards = 0;
        
        for (Map<String, Object> card : cards) {
            boolean isActive = (Boolean) card.get("isActive");
            if (isActive) activeCards++;
            
            Object[] row = {
                card.get("cardId"),
                card.get("cardNickname"),
                card.get("cardNumber"),
                "****", // Hide PIN in display
                String.format("₹%.2f", (Double) card.get("spendingLimit")),
                card.get("usageCount"),
                card.get("status"),
                formatTimestamp((Timestamp) card.get("expiresAt"))
            };
            
            tableModel.addRow(row);
        }
        
        // Update stats
        totalCardsLabel.setText("Total Cards: " + totalCards);
        activeCardsLabel.setText("Active Cards: " + activeCards);
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "No Expiry";
        return ts.toString().substring(0, 10); // YYYY-MM-DD format
    }

    private void addActions() {
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateCardDialog();
            }
        });

        transactionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTransactionDialog();
            }
        });

        deactivateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deactivateSelectedCard();
            }
        });

        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCards();
                JOptionPane.showMessageDialog(
                    VirtualCardsPanel.this,
                    "Cards data refreshed!",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void showCreateCardDialog() {
        JDialog dialog = new JDialog(this, "Create New Virtual Card", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Card Name
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Card Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(15);
        dialog.add(nameField, gbc);
        
        // Card Type
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Card Type:"), gbc);
        gbc.gridx = 1;
        String[] types = {"Shopping", "Subscription", "Travel", "Business", "One-time"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        dialog.add(typeCombo, gbc);
        
        // Spending Limit
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Spending Limit (₹):"), gbc);
        gbc.gridx = 1;
        JTextField limitField = new JTextField(15);
        dialog.add(limitField, gbc);
        
        // Validity Days
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Validity (Days):"), gbc);
        gbc.gridx = 1;
        JTextField validityField = new JTextField("30", 15);
        dialog.add(validityField, gbc);
        
        // Temporary Card
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Temporary Card:"), gbc);
        gbc.gridx = 1;
        JCheckBox tempCheck = new JCheckBox();
        dialog.add(tempCheck, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create Card");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText().trim();
                    String type = (String) typeCombo.getSelectedItem();
                    double limit = Double.parseDouble(limitField.getText().trim());
                    int validity = Integer.parseInt(validityField.getText().trim());
                    boolean isTemp = tempCheck.isSelected();
                    
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a card name");
                        return;
                    }
                    
                    Map<String, Object> result = cardManager.createVirtualCard(name, type, limit, isTemp, validity);
                    
                    if ((Boolean) result.get("success")) {
                        String message = String.format(
                            "Virtual Card Created Successfully!\n\n" +
                            "Card Number: %s\n" +
                            "Card PIN: %s\n" +
                            "Expires: %s\n" +
                            "Limit: ₹%.2f",
                            result.get("cardNumber"),
                            result.get("cardPin"),
                            result.get("expiresAt") != null ? result.get("expiresAt").toString() : "No Expiry",
                            limit
                        );
                        
                        JOptionPane.showMessageDialog(dialog, message, "Card Created", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCards();
                    } else {
                        JOptionPane.showMessageDialog(dialog, result.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for limit and validity");
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void showTransactionDialog() {
        int selectedRow = cardsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a card first");
            return;
        }
        
        int cardId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String cardName = (String) tableModel.getValueAt(selectedRow, 1);
        
        JDialog dialog = new JDialog(this, "Make Transaction - " + cardName, true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Amount (₹):"), gbc);
        gbc.gridx = 1;
        JTextField amountField = new JTextField(15);
        dialog.add(amountField, gbc);
        
        // Merchant
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Merchant:"), gbc);
        gbc.gridx = 1;
        JTextField merchantField = new JTextField(15);
        dialog.add(merchantField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextField descField = new JTextField(15);
        dialog.add(descField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton processButton = new JButton("Process Transaction");
        JButton cancelButton = new JButton("Cancel");
        
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String merchant = merchantField.getText().trim();
                    String description = descField.getText().trim();
                    
                    if (merchant.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Please enter merchant name");
                        return;
                    }
                    
                    Map<String, Object> result = cardManager.processTransaction(cardId, amount, merchant, description);
                    
                    if ((Boolean) result.get("success")) {
                        String message = String.format(
                            "Transaction Successful!\n\n" +
                            "Amount: ₹%.2f\n" +
                            "Merchant: %s\n" +
                            "Usage Count: %d",
                            amount,
                            merchant,
                            (Integer) result.get("usageCount")
                        );
                        
                        JOptionPane.showMessageDialog(dialog, message, "Transaction Complete", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadCards();
                    } else {
                        JOptionPane.showMessageDialog(dialog, result.get("message"), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid amount");
                }
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(processButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void deactivateSelectedCard() {
        int selectedRow = cardsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a card to deactivate");
            return;
        }
        
        int cardId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String cardName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to deactivate card: " + cardName + "?",
            "Confirm Deactivation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = cardManager.deactivateCard(cardId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Card deactivated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCards();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to deactivate card", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Test main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Could not set look and feel: " + e.getMessage());
            }
            
            VirtualCardsPanel panel = new VirtualCardsPanel(1);
            panel.setVisible(true);
        });
    }
}