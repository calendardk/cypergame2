package com.cybergame.model.entity;

import com.cybergame.model.enums.OrderStatus;
import com.cybergame.model.enums.PaymentSource;
import com.cybergame.model.enums.SessionStatus;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Session implements Serializable {

    private int sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private SessionStatus status;

    private Account account;
    private Computer computer;
    private List<OrderItem> orderItems;

    public Session(int id, Account acc, Computer comp) {
        this.sessionId = id;
        this.account = acc;
        this.computer = comp;
        this.startTime = LocalDateTime.now();
        this.status = SessionStatus.RUNNING;
        this.orderItems = new ArrayList<>();
    }

    public void end() {
        endTime = LocalDateTime.now();
        status = SessionStatus.CLOSED;
    }

    public int getSessionId() {
        return sessionId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Account getAccount() {
        return account;
    }

    public Computer getComputer() {
        return computer;
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
    }

    public double calcDurationHours() {
        LocalDateTime end = (endTime == null)
            ? LocalDateTime.now()
            : endTime;

        long seconds = Duration.between(startTime, end).getSeconds();
        return seconds / 3600.0;
    }


    public double calcTimeCost() {
        return calcDurationHours()
                * computer.getPricePerHour()
                * account.getTimeDiscountRate();
    }

    public double calcServiceTotalFromAccount() {
        return orderItems.stream()
            .filter(o -> o.getPaymentSource() == PaymentSource.ACCOUNT)
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .mapToDouble(OrderItem::getCost)
            .sum() * account.getServiceDiscountRate();
    }

    public double calcServiceTotalCash() {
        return orderItems.stream()
            .filter(o -> o.getPaymentSource() == PaymentSource.CASH)
            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
            .mapToDouble(OrderItem::getCost)
            .sum();
    }

    public double calcTotalFromAccount() {
        return calcTimeCost() + calcServiceTotalFromAccount();
    }

    public double calcTotalAll() {
        return calcTotalFromAccount() + calcServiceTotalCash();
    }
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
}