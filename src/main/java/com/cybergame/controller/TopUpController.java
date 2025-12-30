package com.cybergame.controller;

import com.cybergame.model.entity.Account;
import com.cybergame.model.entity.TopUpHistory;
import com.cybergame.repository.AccountRepository;
import com.cybergame.repository.TopUpHistoryRepository;

import java.time.LocalDateTime;

/**
 * Controller xử lý nghiệp vụ NẠP TIỀN
 * - Cộng tiền cho account
 * - Ghi lịch sử nạp tiền (employee / admin)
 */
public class TopUpController {

    private final AccountRepository accountRepo;
    private final TopUpHistoryRepository historyRepo;

    public TopUpController(AccountRepository accountRepo,
                           TopUpHistoryRepository historyRepo) {
        this.accountRepo = accountRepo;
        this.historyRepo = historyRepo;
    }

    /**
     * Nạp tiền cho tài khoản
     *
     * @param acc            account cần nạp
     * @param operatorType   "EMPLOYEE" | "ADMIN"
     * @param operatorId     id nhân viên (null nếu admin)
     * @param operatorName   tên người thao tác
     * @param amount         số tiền nạp
     * @param note           ghi chú (có thể null)
     */
    public void topUp(Account acc,
                      String operatorType,
                      Integer operatorId,
                      String operatorName,
                      double amount,
                      String note) {

        // ===== VALIDATE =====
        if (acc == null) {
            throw new IllegalArgumentException("Account không hợp lệ");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải > 0");
        }
        if (operatorType == null || operatorType.isBlank()) {
            throw new IllegalArgumentException("Thiếu loại người thao tác");
        }

        // ===== 1. CỘNG TIỀN =====
        acc.topUp(amount);
        accountRepo.save(acc);

        // ===== 2. GHI LỊCH SỬ =====
        TopUpHistory h = new TopUpHistory();
        h.setAccountId(acc.getUserId());
        h.setAccountName(acc.getUsername());

        h.setOperatorType(operatorType);   // EMPLOYEE / ADMIN
        h.setOperatorId(operatorId);       // null nếu admin
        h.setOperatorName(operatorName);

        h.setAmount(amount);
        h.setCreatedAt(LocalDateTime.now());
        h.setNote(note);

        historyRepo.save(h);
    }
}
