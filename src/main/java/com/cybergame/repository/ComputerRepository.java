package com.cybergame.repository;

import com.cybergame.model.entity.Computer;
import java.util.List;

public interface ComputerRepository {
    void save(Computer c);
    void delete(Computer c);
    List<Computer> findAll();
}
