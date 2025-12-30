package com.cybergame.model.entity;

public class Account extends Userbase {

    private double balance;
    private boolean locked;
    private boolean vip;

    private double timeDiscountRate;
    private double serviceDiscountRate;

    public Account(int id, String username, String password,
                   String displayName, String phone, boolean vip) {
        this.userId = id;
        this.username = username;
        this.passwordHash = password;
        this.displayName = displayName;
        this.phone = phone;
        this.vip = vip;

        this.balance = 0;
        this.locked = false;
        this.timeDiscountRate = vip ? 0.9 : 1.0;
        this.serviceDiscountRate = vip ? 0.95 : 1.0;
    }

    public void topUp(double amount) {
        if (amount > 0) balance += amount;
    }

    public boolean canPay(double amount) {
        return balance >= amount;
    }

    public void deduct(double amount) {
        if (canPay(amount)) balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public double getTimeDiscountRate() {
        return timeDiscountRate;
    }

    public double getServiceDiscountRate() {
        return serviceDiscountRate;
    }
}
