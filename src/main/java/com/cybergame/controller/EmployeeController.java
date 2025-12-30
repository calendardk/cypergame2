package com.cybergame.controller;

import com.cybergame.model.entity.Employee;
import com.cybergame.repository.EmployeeRepository;

public class EmployeeController {

    private final EmployeeRepository repo;
    private int nextId;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
        this.nextId = repo.findAll()
                .stream()
                .mapToInt(Employee::getUserId)
                .max()
                .orElse(0) + 1;
    }

    public Employee createEmployee(String username,
                                   String password,
                                   String displayName,
                                   String phone) {

        Employee e = new Employee(
                nextId++, username, password, displayName, phone
        );
        repo.save(e);
        return e;
    }

    public void delete(Employee e) {
        repo.delete(e);
    }
    public void lock(Employee e) {
        e.lock();
        repo.save(e);
    }

    public void unlock(Employee e) {
        e.unlock();
        repo.save(e);
    }

}
