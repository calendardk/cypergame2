package com.cybergame.model.entity;

import com.cybergame.model.enums.ComputerStatus;
import java.io.Serializable;

public class Computer implements Serializable {

    private int computerId;
    private String name;
    private double pricePerHour;
    private ComputerStatus status;

    public Computer(int id, String name, double pricePerHour) {
        this.computerId = id;
        this.name = name;
        this.pricePerHour = pricePerHour;
        this.status = ComputerStatus.AVAILABLE;
    }

    public void markInUse() {
        status = ComputerStatus.IN_USE;
    }

    public void markAvailable() {
        status = ComputerStatus.AVAILABLE;
    }

    public ComputerStatus getStatus() {
        return status;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public String getName() {
        return name;
    }

    public int getComputerId() {
        return computerId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public void setStatus(ComputerStatus status) {
        this.status = status;
    }
    
}
