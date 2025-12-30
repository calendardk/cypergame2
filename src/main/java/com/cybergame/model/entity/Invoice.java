package com.cybergame.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


public class Invoice implements Serializable {

    private int invoiceId;
    private int accountId;
    private String computerName;
    private LocalDateTime createdAt;
    private double timeAmount;     // tiền máy
    private double serviceAccountAmount; // order trả bằng account
    private double serviceCashAmount;    // order trả bằng tiền mặt
    private double serviceAmount;  // tiền dịch vụ
    private double totalAmount;
    private List<OrderItem> orderItems;
    private String accountName;

    public Invoice(int id, Session session) {
        this.invoiceId = id;
        this.accountId = session.getAccount().getUserId();
        this.accountName = session.getAccount().getUsername();
        this.computerName = session.getComputer().getName();
        this.createdAt = LocalDateTime.now();
        this.timeAmount = session.calcTimeCost();
        this.serviceAccountAmount = session.calcServiceTotalFromAccount();
        this.serviceCashAmount = session.calcServiceTotalCash();

        this.serviceAmount =
            serviceAccountAmount + serviceCashAmount;

        this.totalAmount =
            timeAmount + serviceAmount;

        this.orderItems = new ArrayList<>(session.getOrderItems());
    }
    public Invoice(int id,
               int accountId,
               String accountName,
               String computerName,
               LocalDateTime createdAt,
               double timeAmount,
               double serviceAmount,
               double serviceAccountAmount,
               double serviceCashAmount,
               double totalAmount) {

    this.invoiceId = id;
    this.accountId = accountId;
    this.accountName = accountName;
    this.computerName = computerName;
    this.createdAt = createdAt;

    this.timeAmount = timeAmount;
    this.serviceAmount = serviceAmount;
    this.serviceAccountAmount = serviceAccountAmount;
    this.serviceCashAmount = serviceCashAmount;
    this.totalAmount = totalAmount;

    this.orderItems = new ArrayList<>();
    }


    public int getAccountId() {
        return accountId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    public int getInvoiceId() {
        return invoiceId;
    }
    public String getComputerName() {
        return computerName;
    }
    public String getAccountName() {
        return accountName;
    }
    public double getTimeAmount() {
        return timeAmount;
    }
    public double getServiceAmount() {
        return serviceAmount;
    }
    public double getServiceAccountAmount() {
        return serviceAccountAmount;
    }
    public double getServiceCashAmount() {
        return serviceCashAmount;
    }

}