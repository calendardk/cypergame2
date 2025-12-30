package com.cybergame.repository;

import com.cybergame.model.entity.ServiceItem;
import java.util.List;

public interface ServiceItemRepository {
    void save(ServiceItem s);
    void delete(ServiceItem s);
    List<ServiceItem> findAll();
}
