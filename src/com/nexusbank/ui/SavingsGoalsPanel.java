package com.nexusbank.ui;

import com.nexusbank.savings.SmartSavingsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * SavingsGoalsPanel - UI for managing savings goals with progress tracking
 */
public class SavingsGoalsPanel extends JFrame {

    private int userId;
    private SmartSavingsManager savingsManager;
    
    private JTable goalsTable;
    private DefaultTableModel tableModel;
    private JButton createBtn, addMoneyBtn, autoTransferBtn, deleteBtn, refreshBtn, closeBtn;
    private JLabel totalGoalsLabel, totalSavedLabel, completedGoalsLabel;
    private JProgressBar overallProgressBar;

    public SavingsGoalsPanel(int userId) {
        this.userId = userId;
        this.savingsManager = new SmartSavingsManager(userId);
        
        setTitle("NexusBank-AI - Smart Savings Goals");
        setSize(1000, 700);
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
        
        loadSavingsGoals();
        addActions();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(16, 185, 129)); // Green color for savings
        header.setPreferredSize(new Dimension(1000, 100));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("💰 Smart Savings Goals");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);
        
        // Stats panel
        JPanel stats = new JPanel(new GridLayout(4, 1, 5, 5));
        stats.setOpaque(false);
        
        totalGoalsLabel = new JLabel("Total Goals: 0");
        totalGoalsLabel.setForeground(Color.WHITE);
        totalGoalsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        completedGoalsLabel = new JLabel("Completed: 0");
        completedGoalsLabel.setForeground(Color.WHITE);
        completedGoalsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        totalSavedLabel = new JLabel("Total Saved: ₹0.00");
        totalSavedLabel.setForeground(Color.WHITE);
        totalSavedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setString("Overall Progress: 0%");
        overallProgressBar.setForeground(new Color(34, 197, 94));
        
        stats.add(totalGoalsLabel);
        stats.add(completedGoalsLabel);
        stats.add(totalSavedLabel);
        stats.add(overallProgressBar);
        header.add(stats, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Goals table
        String[] columns = {"Goal ID", "Goal Name", "Description", "Target (₹)", "Current (₹)", "Progress", "Target Date", "Category", "Auto Transfer", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        goalsTable = new JTable(tableModel);
        goalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goalsTable.setRowHeight(30);
        goalsTable.getTableHeader().setBackground(new Color(229, 231, 235));
        
        // Set column widths
        goalsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        goalsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        goalsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        goalsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        goalsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        goalsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        goalsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        goalsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        goalsTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        goalsTable.getColumnModel().getColumn(9).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(goalsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Your Savings Goals"));
        
        main.add(scrollPane, BorderLayout.CENTER);
        
        return main;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        createBtn = new JButton("Create New Goal");
        createBtn.setBackground(new Color(34, 197, 94));
        createBtn.setForeground(Color.WHITE);
        
        addMoneyBtn = new JButton("Add Money");
        addMoneyBtn.setBackground(new Color(59, 130, 246));
        addMoneyBtn.setForeground(Color.WHITE);
        
        autoTransferBtn = new JButton("Process Auto Transfers");
        autoTransferBtn.setBackground(new Color(168, 85, 247));
        autoTransferBtn.setForeground(Color.WHITE);
        
        deleteBtn = new JButton("Delete Goal");
        deleteBtn.setBackground(new Color(239, 68, 68));
        deleteBtn.setForeground(Color.WHITE);
        
        refreshBtn = new JButton("Refresh");
        closeBtn = new JButton("Close");
        
        panel.add(createBtn);
        panel.add(addMoneyBtn);
        panel.add(autoTransferBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);
        panel.add(closeBtn);
        
        return panel;
    }

    private void loadSavingsGoals() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        List<Map<String, Object>> goals = savingsManager.getUserSavingsGoals();
        
        for (Map<String, Object> goal : goals) {
            String progressText = String.format("%.1f%%", (Double) goal.get("progressPercentage"));
            String autoTransferText = (Boolean) goal.get("autoTransfer") ? 
                "₹" + goal.get("transferAmount") : "No";
            
            Object[] row = {
                goal.get("goalId"),
                goal.get("goalName"),
                goal.get("description"),
                String.format("₹%.2f", (Double) goal.get("targetAmount")),
                String.format("₹%.2f", (Double) goal.get("currentAmount")),
                progressText,
                goal.get("targetDate"),
                goal.get("category"),
                autoTransferText,
                goal.get("status")
            };
            
            tableModel.addRow(row);
        }
        
        // Update statistics
        updateStatistics();
    }

    private void updateStatistics() {
        Map<String, Object> stats = savingsManager.getSavingsStatistics();
        
        int totalGoals = (Integer) stats.getOrDefault("totalGoals", 0);
        int completedGoals = (Integer) stats.getOrDefault("completedGoals", 0);
        double totalSaved = (Double) stats.getOrDefault("totalSaved", 0.0);
        double overallProgress = (Double) stats.getOrDefault("overallProgress", 0.0);
        
        totalGoalsLabel.setText("Total Goals: " + totalGoals);
        completedGoalsLabel.setText("Completed: " + completedGoals);
        totalSavedLabel.setText(String.format("Total Saved: ₹%.2f", totalSaved));
        
        overallProgressBar.setValue((int) overallProgress);
        overallProgressBar.setString(String.format("Overall Progress: %.1f%%", overallProgress));
    }

    private void addActions() {
        createBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCreateGoalDialog();
            }
        });

        addMoneyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddMoneyDialog();
            }
        });

        autoTransferBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processAutoTransfers();
            }
        });

        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedGoal();
            }
        });

        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSavingsGoals();
                JOptionPane.showMessageDialog(
                    SavingsGoalsPanel.this,
                    "Savings goals data refreshed!",
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

    private void showCreateGoalDialog() {
        JDialog dialog = new JDialog(this, "Create New Savings Goal", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Goal Name
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Goal Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        dialog.add(nameField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextField descField = new JTextField(20);
        dialog.add(descField, gbc);
        
        // Target Amount
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Target Amount (₹):"), gbc);
        gbc.gridx = 1;
        JTextField amountField = new JTextField(20);
        dialog.add(amountField, gbc);
        
        // Target Date
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Target Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField dateField = new JTextField(20);
        dialog.add(dateField, gbc);
        
        // Category
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        String[] categories = {"Emergency Fund", "Vacation", "Car", "House", "Education", "Investment", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        dialog.add(categoryCombo, gbc);
        
        // Auto Transfer
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Enable Auto Transfer:"), gbc);
        gbc.gridx = 1;
        JCheckBox autoTransferCheck = new JCheckBox();
        dialog.add(autoTransferCheck, gbc);
        
        // Transfer Amount
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Transfer Amount (₹):"), gbc);
        gbc.gridx = 1;
        JTextField transferField = new JTextField(20);
        transferField.setEnabled(false);
        dialog.add(transferField, gbc);
        
        // Enable/disable transfer amount based on checkbox
        autoTransferCheck.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transferField.setEnabled(autoTransferCheck.isSelected());
            }
        });
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create Goal");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText().trim();
                    String description = descField.getText().trim();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String date = dateField.getText().trim();
                    String category = (String) categoryCombo.getSelectedItem();
                    boolean autoTransfer = autoTransferCheck.isSelected();
                    double transferAmount = 0;
                    
                    if (autoTransfer) {
                        transferAmount = Double.parseDouble(transferField.getText().trim());
                    }
                    
                    if (name.isEmpty() || description.isEmpty() || date.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Please fill all required fields");
                        return;
                    }
                    
                    Map<String, Object> result = savingsManager.createSavingsGoal(
                        name, description, amount, date, category, autoTransfer, transferAmount
                    );
                    
                    if ((Boolean) result.get("success")) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Savings goal created successfully!\nGoal ID: " + result.get("goalId"), 
                            "Goal Created", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        dialog.dispose();
                        loadSavingsGoals();
                    } else {
                        JOptionPane.showMessageDialog(dialog, result.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for amount fields");
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void showAddMoneyDialog() {
        int selectedRow = goalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a goal first");
            return;
        }
        
        int goalId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String goalName = (String) tableModel.getValueAt(selectedRow, 1);
        
        JDialog dialog = new JDialog(this, "Add Money to Goal - " + goalName, true);
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Amount to Add (₹):"), gbc);
        gbc.gridx = 1;
        JTextField amountField = new JTextField(15);
        dialog.add(amountField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        JTextField descField = new JTextField(15);
        descField.setText("Manual deposit");
        dialog.add(descField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Money");
        JButton cancelButton = new JButton("Cancel");
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String description = descField.getText().trim();
                    
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(dialog, "Please enter a positive amount");
                        return;
                    }
                    
                    Map<String, Object> result = savingsManager.addToSavingsGoal(goalId, amount, description);
                    
                    if ((Boolean) result.get("success")) {
                        String message = String.format(
                            "%s\n\nNew Amount: ₹%.2f\nProgress: %.1f%%",
                            result.get("message"),
                            (Double) result.get("newAmount"),
                            (Double) result.get("progressPercentage")
                        );
                        
                        JOptionPane.showMessageDialog(dialog, message, "Money Added", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadSavingsGoals();
                    } else {
                        JOptionPane.showMessageDialog(dialog, result.get("message"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid amount");
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }

    private void processAutoTransfers() {
        Map<String, Object> result = savingsManager.processAutoTransfers();
        
        String message = String.format(
            "Auto Transfer Results:\n\n%s",
            result.get("message")
        );
        
        JOptionPane.showMessageDialog(this, message, "Auto Transfers", JOptionPane.INFORMATION_MESSAGE);
        loadSavingsGoals(); // Refresh to show updated amounts
    }

    private void deleteSelectedGoal() {
        int selectedRow = goalsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a goal to delete");
            return;
        }
        
        int goalId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String goalName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentAmount = (String) tableModel.getValueAt(selectedRow, 4);
        
        String message = "Are you sure you want to delete goal: " + goalName + "?";
        if (!currentAmount.equals("₹0.00")) {
            message += "\n\nCurrent savings (" + currentAmount + ") will be returned to your main account.";
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            message,
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = savingsManager.deleteSavingsGoal(goalId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Goal deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSavingsGoals();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete goal", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Test main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception e) {
                    System.out.println("Could not set look and feel: " + e.getMessage());
                }
                
                SavingsGoalsPanel panel = new SavingsGoalsPanel(1);
                panel.setVisible(true);
            }
        });
    }
}