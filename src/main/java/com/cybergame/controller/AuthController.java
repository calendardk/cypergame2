package com.cybergame.controller;

import com.cybergame.model.entity.*;
import com.cybergame.repository.AccountRepository;

public class AuthController {

    private final AccountRepository accountRepo;
    private final SessionManager sessionManager;

    public AuthController(AccountRepository repo,
                          SessionManager sessionManager) {
        this.accountRepo = repo;
        this.sessionManager = sessionManager;
    }

    public Session loginCustomer(String username,
                                 String password,
                                 Computer computer) {

        Account acc = accountRepo.findByUsername(username);
        if (acc == null) return null;
        if (acc.isLocked()) return null;
        if (!acc.login(password)) return null;

        return sessionManager.startSession(acc, computer);
    }

    public void logout(Session session) {
        sessionManager.endSession(session);
    }
}
