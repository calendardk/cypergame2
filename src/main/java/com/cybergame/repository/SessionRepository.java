package com.cybergame.repository;

import com.cybergame.model.entity.Session;
import java.util.List;

public interface SessionRepository {
    void save(Session s);
    void delete(Session s);
    List<Session> findRunningSessions();
}
