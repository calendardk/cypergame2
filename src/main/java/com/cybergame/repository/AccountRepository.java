package com.cybergame.repository;

import com.cybergame.model.entity.Account;
import java.util.List;

public interface AccountRepository {
    void save(Account acc);
    void delete(Account acc);
    Account findByUsername(String username);
    List<Account> findAll();
}
