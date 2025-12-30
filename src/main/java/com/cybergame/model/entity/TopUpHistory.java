package com.cybergame.model.entity;

import java.time.LocalDateTime;

public class TopUpHistory {

    private int id;

    private int accountId;
    private String accountName;

    private String operatorType;   // ADMIN | EMPLOYEE
    private Integer operatorId;    // NULL nếu admin
    private String operatorName;

    private double amount;
    private LocalDateTime createdAt;
    private String note;
    public int getId() {
        return id;
    }
    public int getAccountId() {
        return accountId;
    }
    public String getAccountName() {
        return accountName;
    }
    public String getOperatorType() {
        return operatorType;
    }
    public Integer getOperatorId() {
        return operatorId;
    }
    public String getOperatorName() {
        return operatorName;
    }
    public double getAmount() {
        return amount;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public String getNote() {
        return note;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }
    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setNote(String note) {
        this.note = note;
    }

    // constructor + getter/setter
}
