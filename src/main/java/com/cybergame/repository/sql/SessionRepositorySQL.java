package com.cybergame.repository.sql;

import com.cybergame.model.entity.Session;
import com.cybergame.repository.SessionRepository;

import java.util.*;

public class SessionRepositorySQL implements SessionRepository {

    private final List<Session> sessions = new ArrayList<>();

    @Override
    public void save(Session s) {
        sessions.add(s);
    }

    @Override
    public void delete(Session s) {
        sessions.remove(s);
    }

    @Override
    public List<Session> findRunningSessions() {
        return new ArrayList<>(sessions);
    }
}
