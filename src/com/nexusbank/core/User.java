package com.nexusbank.core;

import java.sql.Timestamp;

/**
 * User Model - Represents a bank customer
 */
public class User {
    
    private int userId;
    private String cardNumber;
    private String primaryPin;
    private String panicPin;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String address;
    private String accountType;
    private String accountStatus;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private String biometricHash;
    private String securityQuestion;
    private String securityAnswer;
    
    // Constructors
    public User() {}
    
    public User(String cardNumber, String primaryPin, String fullName) {
        this.cardNumber = cardNumber;
        this.primaryPin = primaryPin;
        this.fullName = fullName;
        this.accountStatus = "ACTIVE";
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    
    public String getPrimaryPin() { return primaryPin; }
    public void setPrimaryPin(String primaryPin) { this.primaryPin = primaryPin; }
    
    public String getPanicPin() { return panicPin; }
    public void setPanicPin(String panicPin) { this.panicPin = panicPin; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
    public String getBiometricHash() { return biometricHash; }
    public void setBiometricHash(String biometricHash) { this.biometricHash = biometricHash; }
    
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String securityQuestion) { this.securityQuestion = securityQuestion; }
    
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String securityAnswer) { this.securityAnswer = securityAnswer; }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", cardNumber='" + cardNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", accountType='" + accountType + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}
