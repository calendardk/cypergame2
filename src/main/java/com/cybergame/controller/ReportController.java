package com.cybergame.controller;

import com.cybergame.model.entity.Invoice;
import com.cybergame.model.entity.OrderItem;
import com.cybergame.repository.InvoiceRepository;

import java.util.ArrayList;
import java.util.List;

public class ReportController {

    private final InvoiceRepository repo;

    public ReportController(InvoiceRepository repo) {
        this.repo = repo;
    }

    public List<Invoice> getAllInvoices() {
        return repo.findAll();
    }
    
    public void deleteInvoice(Invoice invoice) {
        repo.delete(invoice);
    }
    public List<OrderItem> getOrderHistoryTable() {

    List<OrderItem> result = new ArrayList<>();

    for (Invoice inv : repo.findAll()) {
        if (inv.getOrderItems() != null) {
            result.addAll(inv.getOrderItems());
        }
    }
    return result;
    }
}
