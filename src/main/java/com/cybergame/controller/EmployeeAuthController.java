package com.cybergame.controller;

import com.cybergame.model.entity.Employee;
import com.cybergame.repository.EmployeeRepository;

public class EmployeeAuthController {

    private final EmployeeRepository employeeRepo;

    public EmployeeAuthController(EmployeeRepository repo) {
        this.employeeRepo = repo;
    }

    public Employee login(String username, String password) {

        Employee emp = employeeRepo.findByUsername(username);
        if (emp == null) return null;

        if (emp.isLocked()) return null;
        if (!emp.login(password)) return null;

        return emp; // 🔥 không tạo session
    }
}
