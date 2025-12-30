package com.cybergame.controller;

import com.cybergame.model.entity.TopUpHistory;
import com.cybergame.repository.TopUpHistoryRepository;

import java.util.List;

public class TopUpHistoryController {

    private final TopUpHistoryRepository repo;

    public TopUpHistoryController(TopUpHistoryRepository repo) {
        this.repo = repo;
    }

    public List<TopUpHistory> getAll() {
        return repo.findAll();
    }

    public List<TopUpHistory> getByAccount(int accountId) {
        return repo.findByAccount(accountId);
    }

    /**
     * ❗ CHỈ XÓA LỊCH SỬ – KHÔNG HOÀN TIỀN
     */
    public void delete(TopUpHistory history) {
        repo.delete(history);
    }
}
