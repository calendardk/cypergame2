package com.cybergame.repository;

import com.cybergame.model.entity.TopUpHistory;
import java.util.List;

public interface TopUpHistoryRepository {

    void save(TopUpHistory history);

    void delete(TopUpHistory history);

    List<TopUpHistory> findAll();

    List<TopUpHistory> findByAccount(int accountId);
}
