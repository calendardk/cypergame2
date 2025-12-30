package com.cybergame.controller;

import com.cybergame.model.entity.*;
import com.cybergame.model.enums.OrderStatus;
import com.cybergame.model.enums.PaymentSource;
import com.cybergame.repository.AccountRepository;


public class OrderController {

    private int nextOrderId = 1;
    private final AccountRepository accountRepo;

    public OrderController(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

public OrderItem addOrder(Session session,
                          ServiceItem service,
                          int quantity,
                          PaymentSource source) {
    if (service.isLocked()) {
    return null;
}


    double orderCost = service.getUnitPrice() * quantity;

if (source == PaymentSource.ACCOUNT) {

    Account acc = session.getAccount();

    if (!acc.canPay(orderCost)) {
        return null;
    }

    acc.deduct(orderCost);

    // ✅ LƯU NGAY SAU KHI TRỪ
    accountRepo.save(acc);
}


    OrderItem item = new OrderItem(
            nextOrderId++, service, quantity, source
    );
    session.addOrderItem(item);
    return item;
    }
public void cancelOrder(OrderItem item, Session session) {

    // chỉ huỷ khi còn pending
    if (item.getStatus() != OrderStatus.PENDING) {
        return;
    }

    // chỉ refund nếu trả bằng account
    if (item.getPaymentSource() == PaymentSource.ACCOUNT) {

        double refundAmount = item.getCost();

        Account acc = session.getAccount();
        acc.topUp(refundAmount);

        // lưu lại số dư sau khi refund
        accountRepo.save(acc);
    }

    item.setStatus(OrderStatus.CANCELLED);
}

    public void confirmOrder(OrderItem item) {
        item.setStatus(OrderStatus.CONFIRMED);
    }

    public void completeOrder(OrderItem item) {
        item.setStatus(OrderStatus.COMPLETED);
    }

}
