package com.cybergame.repository;

import com.cybergame.model.entity.Invoice;
import java.util.List;

public interface InvoiceRepository {
    void save(Invoice i);
    void delete(Invoice i);
    List<Invoice> findAll();
}
