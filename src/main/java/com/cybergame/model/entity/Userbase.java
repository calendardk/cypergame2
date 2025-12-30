package com.cybergame.model.entity;

import java.io.Serializable;

public abstract class Userbase implements Serializable {

    protected int userId;
    protected String username;
    protected String passwordHash;
    protected String displayName;
    protected String phone;

    public boolean login(String password) {
        return passwordHash.equals(password); // demo
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}