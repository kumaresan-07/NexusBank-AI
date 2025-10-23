package com.nexusbank.ui;

import com.nexusbank.core.DatabaseManager;
import com.nexusbank.gamification.RewardsEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen extends JFrame {

    private JTextField cardField;
    private JPasswordField pinField;
    private JButton loginBtn, signupBtn, biometricBtn;

    public LoginScreen() {
        setTitle("NexusBank-AI - Login");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(37,99,235));
        header.setPreferredSize(new Dimension(420,60));
        JLabel title = new JLabel("NexusBank-AI");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel cardLabel = new JLabel("Card Number:");
        gbc.gridx = 0; gbc.gridy = 0;
        center.add(cardLabel, gbc);

        cardField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        center.add(cardField, gbc);

        JLabel pinLabel = new JLabel("PIN:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        center.add(pinLabel, gbc);

        pinField = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        center.add(pinField, gbc);

        loginBtn = new JButton("Login");
        signupBtn = new JButton("Sign Up");
        biometricBtn = new JButton("Biometric Login");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        btnPanel.add(biometricBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        center.add(btnPanel, gbc);

        add(center, BorderLayout.CENTER);

        addActions();
    }

    private void addActions() {
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String card = cardField.getText().trim();
                String pin = new String(pinField.getPassword()).trim();

                if (card.isEmpty() || pin.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginScreen.this, "Enter card number and PIN", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validate using DatabaseManager
                DatabaseManager db = DatabaseManager.getInstance();
                try {
                    String q = String.format("SELECT user_id, full_name FROM users WHERE card_number='%s' AND primary_pin='%s'", card, pin);
                    ResultSet rs = db.executeQuery(q);
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String name = rs.getString("full_name");
                        rs.close();

                        // Award daily login bonus via RewardsEngine
                        RewardsEngine rewards = new RewardsEngine(userId);
                        rewards.awardDailyLoginBonus();

                        // Open Dashboard
                        Dashboard dashboard = new Dashboard(userId, name);
                        dashboard.setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginScreen.this, "Invalid card or PIN", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginScreen.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Sign Up flow not yet implemented. We'll scaffold it next.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        biometricBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Biometric login not yet implemented. Coming soon.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginScreen ls = new LoginScreen();
            ls.setVisible(true);
        });
    }
}
