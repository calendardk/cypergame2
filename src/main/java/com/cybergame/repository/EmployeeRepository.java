package com.cybergame.repository;

import com.cybergame.model.entity.Employee;
import java.util.List;

public interface EmployeeRepository {
    void save(Employee e);
    void delete(Employee e);
    Employee findByUsername(String username);
    List<Employee> findAll();
}
