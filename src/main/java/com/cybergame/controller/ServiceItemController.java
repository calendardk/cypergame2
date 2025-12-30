package com.cybergame.controller;

import com.cybergame.model.entity.ServiceItem;
import com.cybergame.model.enums.ServiceCategory;
import com.cybergame.repository.ServiceItemRepository;

public class ServiceItemController {

    private final ServiceItemRepository repo;
    private int nextId;

    public ServiceItemController(ServiceItemRepository repo) {
        this.repo = repo;
        this.nextId = repo.findAll()
                .stream()
                .mapToInt(ServiceItem::getServiceId)
                .max()
                .orElse(0) + 1;
    }

    public ServiceItem createService(String name,
                                 double price,
                                 ServiceCategory category) {

    ServiceItem s = new ServiceItem(
            nextId++, name, price, category
    );
    repo.save(s);
    return s;
    }

    public void delete(ServiceItem s) {
        repo.delete(s);
    }
    public void lock(ServiceItem s) {
        s.lock();
        repo.save(s);
    }

    public void unlock(ServiceItem s) {
        s.unlock();
        repo.save(s);
    }

}
