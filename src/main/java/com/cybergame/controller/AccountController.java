package com.cybergame.controller;

import com.cybergame.model.entity.Account;
import com.cybergame.repository.AccountRepository;

public class AccountController {

    private final AccountRepository repo;
    private int nextId;

    public AccountController(AccountRepository repo) {
        this.repo = repo;

        // ✅ KHỞI TẠO nextId = maxId + 1
        this.nextId = repo.findAll()
                .stream()
                .mapToInt(Account::getUserId)
                .max()
                .orElse(0) + 1;
    }

    public Account createAccount(String username,
                                 String password,
                                 String displayName,
                                 String phone,
                                 boolean vip) {

        Account acc = new Account(
                nextId++, username, password, displayName,phone, vip
        );
        repo.save(acc);
        return acc;
    }


    public void topUp(Account acc, double amount) {
        acc.topUp(amount);
        repo.save(acc);
    }

    public void lock(Account acc) {
        acc.lock();
        repo.save(acc);
    }

    public void unlock(Account acc) {
        acc.unlock();
        repo.save(acc);
    }

    public void delete(Account acc) {
        repo.delete(acc);
    }
}
