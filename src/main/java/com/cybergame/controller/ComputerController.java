package com.cybergame.controller;

import com.cybergame.model.entity.Computer;
import com.cybergame.repository.ComputerRepository;

public class ComputerController {

    private final ComputerRepository repo;
    private int nextId;

    public ComputerController(ComputerRepository repo) {
        this.repo = repo;

        // ✅ KHỞI TẠO nextId = maxId + 1
        this.nextId = repo.findAll()
                .stream()
                .mapToInt(Computer::getComputerId)
                .max()
                .orElse(0) + 1;
    }

    public Computer createComputer(String name, double pricePerHour) {

        Computer c = new Computer(
                nextId++, name, pricePerHour
        );
        repo.save(c);
        return c;
    }

    public void delete(Computer c) {
        repo.delete(c);
    }
}
