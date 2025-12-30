package com.cybergame.model.entity;

import java.io.Serializable;
import com.cybergame.model.enums.ServiceCategory;

public class ServiceItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int serviceId;
    private String name;
    private double unitPrice;
    private boolean locked;
    private ServiceCategory category;

public ServiceItem(int id, String name, double price, ServiceCategory category) {
    this.serviceId = id;
    this.name = name;
    this.unitPrice = price;
    this.category = category;
}


    public double getUnitPrice() {
        return unitPrice;
    }

    public String getName() {
        return name;
    }
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
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

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }
}