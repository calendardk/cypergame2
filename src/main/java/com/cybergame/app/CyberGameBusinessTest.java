package com.cybergame.app;

import com.cybergame.context.AccountContext;
import com.cybergame.controller.*;
import com.cybergame.model.entity.*;
import com.cybergame.model.enums.PaymentSource;
import com.cybergame.model.enums.ServiceCategory;
import com.cybergame.repository.*;
import com.cybergame.repository.sql.*;

import java.util.List;

public class CyberGameBusinessTest {

    public static void main(String[] args) throws Exception {

        // ================= INIT DB =================
        DBInit.init();

        // ================= REPOSITORIES =================
        AccountRepository accountRepo = new AccountRepositorySQL();
        EmployeeRepository employeeRepo = new EmployeeRepositorySQL();
        ComputerRepository computerRepo = new ComputerRepositorySQL();
        ServiceItemRepository serviceRepo = new ServiceItemRepositorySQL();
        InvoiceRepository invoiceRepo = new InvoiceRepositorySQL();
        TopUpHistoryRepository topUpRepo = new TopUpHistoryRepositorySQL();
        SessionRepository sessionRepo = new SessionRepositorySQL();

        // ================= CONTROLLERS =================
        AccountController accountController = new AccountController(accountRepo);
        EmployeeController employeeController = new EmployeeController(employeeRepo);
        ComputerController computerController = new ComputerController(computerRepo);
        ServiceItemController serviceController = new ServiceItemController(serviceRepo);
        TopUpController topUpController = new TopUpController(accountRepo, topUpRepo);
        SessionManager sessionManager = new SessionManager(sessionRepo, invoiceRepo, accountRepo);
        AuthController authController = new AuthController(accountRepo, sessionManager);
        OrderController orderController = new OrderController(accountRepo);
        ReportController reportController = new ReportController(invoiceRepo);

        AccountContext context = AccountContext.getInstance();

        // ==================================================
        System.out.println("===== 1. SETUP DỮ LIỆU =====");

        // 10 khách
        for (int i = 1; i <= 10; i++) {
            accountController.createAccount(
                    "user" + i,
                    "123",
                    "User " + i,
                    "09000000" + i,
                    false
            );
        }

        // 5 nhân viên
        for (int i = 1; i <= 5; i++) {
            employeeController.createEmployee(
                    "emp" + i,
                    "123",
                    "Employee " + i,
                    "09111111" + i
            );
        }

        // 10 máy
        for (int i = 1; i <= 10; i++) {
            computerController.createComputer("PC-" + i, 10000);
        }

        // 5 dịch vụ + CATEGORY
        ServiceItem coca = serviceController.createService(
                "Coca", 15000, ServiceCategory.DRINK
        );
        ServiceItem cafe = serviceController.createService(
                "Cafe", 30000, ServiceCategory.DRINK
        );
        ServiceItem mi = serviceController.createService(
                "Mi ly", 25000, ServiceCategory.FOOD
        );
        ServiceItem snack = serviceController.createService(
                "Snack", 20000, ServiceCategory.SNACK
        );
        ServiceItem banh = serviceController.createService(
                "Banh ngot", 18000, ServiceCategory.SNACK
        );

        // ==================================================
        System.out.println("\n===== 2. HIỂN THỊ DỮ LIỆU =====");

        accountRepo.findAll()
                .forEach(a -> System.out.println("Account: " + a.getUsername()));

        employeeRepo.findAll()
                .forEach(e -> System.out.println("Employee: " + e.getUsername()));

        computerRepo.findAll()
                .forEach(c -> System.out.println("Computer: " + c.getName()));

        serviceRepo.findAll()
                .forEach(s -> System.out.println(
                        "Service: " + s.getName()
                                + " | Category: " + s.getCategory()
                                + " | Locked: " + s.isLocked()
                ));

        // ==================================================
        System.out.println("\n===== 3. LOGIN + NẠP TIỀN BAN ĐẦU =====");

        Computer pc1 = computerRepo.findAll().get(0);
        Computer pc2 = computerRepo.findAll().get(1);

        Session s1 = authController.loginCustomer("user1", "123", pc1);
        Session s2 = authController.loginCustomer("user2", "123", pc2);

        // put context khi login
        context.put(s1.getAccount());
        context.put(s2.getAccount());

        // nạp tiền ban đầu
        topUp(context, accountRepo, topUpController,
                "user1", 1, "Employee 1", 200_000, "Nap dau");

        topUp(context, accountRepo, topUpController,
                "user2", 2, "Employee 2", 150_000, "Nap dau");

        // ==================================================
        System.out.println("\n===== 4. CHƠI GAME + ORDER + NẠP TIỀN GIỮA GIỜ =====");

        Thread.sleep(3000);

        // user1 order (cash + account)
        orderController.addOrder(s1, coca, 1, PaymentSource.ACCOUNT);
        orderController.addOrder(s1, snack, 2, PaymentSource.CASH);
        orderController.addOrder(s1, mi, 1, PaymentSource.ACCOUNT);

        // nạp tiền giữa giờ (đang online → dùng context)
        topUp(context, accountRepo, topUpController,
                "user1", 3, "Employee 3", 100_000, "Nap giua gio");

        Thread.sleep(2000);

        // ==================================================
        System.out.println("\n===== 5. LOGOUT =====");

        authController.logout(s1);
        context.remove("user1");

        authController.logout(s2);
        context.remove("user2");

        // ==================================================
        System.out.println("\n===== 6. IN HÓA ĐƠN (SNAPSHOT) =====");

        for (Invoice inv : reportController.getAllInvoices()) {
            System.out.println("Invoice #" + inv.getInvoiceId());
            System.out.println(" User: " + inv.getAccountName());
            System.out.println(" PC: " + inv.getComputerName());

            System.out.println(" Time amount: " + inv.getTimeAmount());

            System.out.println(" Service (ACCOUNT): " + inv.getServiceAccountAmount());
            System.out.println(" Service (CASH): " + inv.getServiceCashAmount());
            System.out.println(" Service (TOTAL): " + inv.getServiceAmount());

            System.out.println(" TOTAL: " + inv.getTotalAmount());
            System.out.println("----------------------------------");
        }

        // ==================================================
        System.out.println("\n===== 7. LỊCH SỬ ORDER =====");

        List<OrderItem> orders = reportController.getOrderHistoryTable();
        for (OrderItem o : orders) {
            System.out.println(
                    "Order #" + o.getOrderItemId()
                            + " | " + o.getServiceItem().getName()
                            + " | Category: " + o.getServiceItem().getCategory()
                            + " | Qty: " + o.getQuantity()
                            + " | Pay: " + o.getPaymentSource()
                            + " | Status: " + o.getStatus()
            );
        }

        System.out.println("\n===== TEST NGHIỆP VỤ HOÀN TẤT =====");
    }

    /**
     * Nạp tiền CHUẨN:
     * - ưu tiên AccountContext (online)
     * - fallback DB nếu offline
     * - KHÔNG sửa controller
     */
    private static void topUp(AccountContext context,
                              AccountRepository accountRepo,
                              TopUpController controller,
                              String username,
                              int empId,
                              String empName,
                              double amount,
                              String note) {

        Account acc = context.get(username);

        if (acc == null) {
            acc = accountRepo.findByUsername(username);
        }

        controller.topUp(
                acc,
                "EMPLOYEE",
                empId,
                empName,
                amount,
                note
        );
    }
}
