package com.cybergame.context;

import com.cybergame.model.entity.Account;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AccountContext (Singleton Pattern)
 * Đóng vai trò là bộ nhớ RAM (In-Memory Storage) lưu trữ trạng thái của các user đang online.
 * Giúp đồng bộ tiền nong real-time mà không cần query DB liên tục.
 */
public class AccountContext {

    // ================== SINGLETON SETUP ==================
    // 1. Tạo sẵn 1 instance duy nhất ngay khi chạy chương trình
    private static final AccountContext INSTANCE = new AccountContext();

    // 2. Chặn không cho bên ngoài tự ý "new AccountContext()"
    private AccountContext() {
    }

    // 3. Cung cấp hàm để lấy instance duy nhất này ra dùng
    public static AccountContext getInstance() {
        return INSTANCE;
    }
    // =====================================================

    // Dùng ConcurrentHashMap để an toàn khi có nhiều luồng (Thread-safe) truy cập cùng lúc
    // Key: Username, Value: Object Account (đang giữ tiền thực tế)
    private final ConcurrentHashMap<String, Account> onlineAccounts = new ConcurrentHashMap<>();

    /**
     * Đưa một Account vào danh sách Online (khi Login thành công)
     */
    public void put(Account account) {
        if (account != null) {
            onlineAccounts.put(account.getUsername(), account);
        }
    }

    /**
     * Lấy Account đang online (để trừ tiền, xem số dư thực tế)
     */
    public Account get(String username) {
        if (username == null) return null;
        return onlineAccounts.get(username);
    }

    /**
     * Xóa Account khỏi danh sách Online (khi Logout)
     */
    public void remove(String username) {
        if (username != null) {
            onlineAccounts.remove(username);
        }
    }

    /**
     * Lấy toàn bộ danh sách (để hiển thị lên bảng Admin)
     */
    public ConcurrentHashMap<String, Account> getAll() {
        return onlineAccounts;
    }
    
    /**
     * Kiểm tra xem user có đang online không
     */
    public boolean isOnline(String username) {
        return onlineAccounts.containsKey(username);
    }
}