package com.cybergame.controller;
import com.cybergame.model.entity.*;
import com.cybergame.model.enums.*;
import com.cybergame.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SessionManager {

    private final SessionRepository sessionRepo;
    private final InvoiceRepository invoiceRepo;
    private final AccountRepository accountRepo;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private int nextSessionId = 1;
    private int nextInvoiceId;

    public SessionManager(SessionRepository sessionRepo,
                      InvoiceRepository invoiceRepo,
                      AccountRepository accountRepo) {
        this.sessionRepo = sessionRepo;
        this.invoiceRepo = invoiceRepo;
        this.accountRepo = accountRepo;

        this.nextInvoiceId = invoiceRepo.findAll()
                .stream()
                .mapToInt(Invoice::getInvoiceId)
                .max()
                .orElse(0) + 1;
                
        startTimer();
    }


    /* ================= TIMER ================= */

    private void startTimer() {
        scheduler.scheduleAtFixedRate(
                this::tickUpdate,
                1, 1, TimeUnit.SECONDS
        );
    }
    private void chargeTimeCostPerSecond(Session session) {

    Account acc = session.getAccount();
    Computer pc = session.getComputer();

    // tiền / giờ → tiền / giây
    double costPerSecond =
            pc.getPricePerHour() / 3600.0
            * acc.getTimeDiscountRate();

    // nếu không đủ tiền → logout
    if (!acc.canPay(costPerSecond)) {
        forceLogout(session);
        return;
    }

    // ✅ TRỪ TIỀN NGAY MỖI GIÂY
    acc.deduct(costPerSecond);
}


    private void tickUpdate() {
        for (Session s : sessionRepo.findRunningSessions()) {
            chargeTimeCostPerSecond(s);
        }
    }

    /* ================= SESSION ================= */

    public Session startSession(Account acc, Computer comp) {
        if (acc.isLocked()) return null;
        if (comp.getStatus() != ComputerStatus.AVAILABLE) return null;

        Session session = new Session(nextSessionId++, acc, comp);
        comp.markInUse();
        sessionRepo.save(session);
        return session;
    }

public void endSession(Session session) {

    // 🔐 CHỐT NGAY – thread khác vào sẽ bị chặn
    if (session.getStatus() == SessionStatus.CLOSED) return;

    session.end(); // ⚠️ ĐẶT CLOSED NGAY LẬP TỨC

    // ===== TỪ ĐÂY CHỈ 1 THREAD ĐƯỢC CHẠY =====

    sessionRepo.delete(session);

    Account acc = session.getAccount();
    accountRepo.save(acc); // ✅ ghi DB 1 lần duy nhất

    session.getComputer().markAvailable();

    Invoice invoice = new Invoice(nextInvoiceId++, session);
    invoiceRepo.save(invoice); // ✅ ghi DB 1 lần duy nhất
}


    private void checkBalanceAndForceLogout(Session session) {
        Account acc = session.getAccount();
        double need = session.calcTotalFromAccount();

        if (!acc.canPay(need)) {
            forceLogout(session);
        }
    }

    public void forceLogout(Session session) {
        endSession(session);
    }
    public List<OrderItem> getPendingOrders() {

    List<OrderItem> result = new ArrayList<>();

    for (Session s : sessionRepo.findRunningSessions()) {
        for (OrderItem item : s.getOrderItems()) {
            if (item.getStatus() == OrderStatus.PENDING) {
                result.add(item);
            }
        }
    }
    return result;
    }

}
