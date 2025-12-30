package com.cybergame.model.entity;

public class Employee extends Userbase {

    private boolean locked;

    public Employee(int id, String username, String password,
                    String displayName, String phone) {
        this.userId = id;
        this.username = username;
        this.passwordHash = password;
        this.displayName = displayName;
        this.phone = phone;
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

}