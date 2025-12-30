package com.cybergame.ui.fxcontroller;

import com.cybergame.model.entity.Invoice;
import com.cybergame.model.entity.OrderItem;
import com.cybergame.model.entity.TopUpHistory;
import com.cybergame.model.enums.PaymentSource;
import com.cybergame.repository.sql.InvoiceRepositorySQL;
import com.cybergame.repository.sql.TopUpHistoryRepositorySQL;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReportController implements Initializable {

    private final InvoiceRepositorySQL invoiceRepo = new InvoiceRepositorySQL();
    private final TopUpHistoryRepositorySQL topUpRepo = new TopUpHistoryRepositorySQL();

    // --- FXML: STATS LABELS ---
    @FXML private Label lblTotalRealRevenue;   // TỔNG THỰC THU (Tiền mặt vào két)
    @FXML private Label lblTotalTopUp;         // Tổng tiền nạp
    @FXML private Label lblMachineRevenue;     // Doanh thu giờ chơi
    @FXML private Label lblServiceRevenue;     // Tổng Doanh thu dịch vụ
    @FXML private Label lblServiceDetail;      // Chi tiết (Cash/Account)

    // --- FXML: FILTER ---
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    // --- TABLE: HÓA ĐƠN ---
    @FXML private TableView<Invoice> tableInvoices;
    @FXML private TableColumn<Invoice, Integer> colInvId;
    @FXML private TableColumn<Invoice, String> colInvTime;
    @FXML private TableColumn<Invoice, String> colInvCustomer;
    @FXML private TableColumn<Invoice, String> colInvComputer;
    @FXML private TableColumn<Invoice, Double> colInvMachine;        // Tiền giờ
    @FXML private TableColumn<Invoice, Double> colInvSvcCash;        // DV (Tiền mặt)
    @FXML private TableColumn<Invoice, Double> colInvSvcAcc;         // DV (Tài khoản)
    @FXML private TableColumn<Invoice, Double> colInvTotal;          // Tổng cộng

    // --- TABLE: CHI TIẾT MÓN ĂN ---
    @FXML private TableView<OrderItem> tableOrderHistory;
    @FXML private TableColumn<OrderItem, String> colOrdTime;
    @FXML private TableColumn<OrderItem, String> colOrdName;
    @FXML private TableColumn<OrderItem, Integer> colOrdQty;
    @FXML private TableColumn<OrderItem, Double> colOrdPrice;
    @FXML private TableColumn<OrderItem, Double> colOrdTotal;
    @FXML private TableColumn<OrderItem, String> colOrdSource;       // Nguồn tiền (Mới)
    @FXML private TableColumn<OrderItem, String> colOrdStatus;       // Trạng thái (Mới)

    // --- TABLE: LỊCH SỬ NẠP TIỀN ---
    @FXML private TableView<TopUpHistory> tableTopUps;
    @FXML private TableColumn<TopUpHistory, Integer> colTopId;
    @FXML private TableColumn<TopUpHistory, String> colTopTime;
    @FXML private TableColumn<TopUpHistory, String> colTopCustomer;
    @FXML private TableColumn<TopUpHistory, String> colTopRole;
    @FXML private TableColumn<TopUpHistory, String> colTopStaffId;
    @FXML private TableColumn<TopUpHistory, String> colTopOperator;
    @FXML private TableColumn<TopUpHistory, Double> colTopAmount;
    @FXML private TableColumn<TopUpHistory, String> colTopNote;

    private final ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private final ObservableList<TopUpHistory> topUpList = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderList = FXCollections.observableArrayList();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInvoiceTable();
        setupOrderTable();
        setupTopUpTable();

        // Mặc định load ngày hiện tại
        dpFrom.setValue(LocalDate.now());
        dpTo.setValue(LocalDate.now());

        loadData();
    }

    // ================== SETUP TABLES ==================
    private void setupInvoiceTable() {
        colInvId.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        colInvTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCreatedAt().format(dtf)));
        colInvCustomer.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        colInvComputer.setCellValueFactory(new PropertyValueFactory<>("computerName"));

        // Tính toán các cột tiền từ OrderItems
        colInvSvcCash.setCellValueFactory(cell -> {
            double cash = 0;
            if (cell.getValue().getOrderItems() != null) {
                cash = cell.getValue().getOrderItems().stream()
                        .filter(o -> o.getPaymentSource() == PaymentSource.CASH)
                        .mapToDouble(OrderItem::getCost).sum();
            }
            return new SimpleDoubleProperty(cash).asObject();
        });
        formatCurrencyColumn(colInvSvcCash, "#fbbf24"); // Vàng (Cash)

        colInvSvcAcc.setCellValueFactory(cell -> {
            double acc = 0;
            if (cell.getValue().getOrderItems() != null) {
                acc = cell.getValue().getOrderItems().stream()
                        .filter(o -> o.getPaymentSource() == PaymentSource.ACCOUNT)
                        .mapToDouble(OrderItem::getCost).sum();
            }
            return new SimpleDoubleProperty(acc).asObject();
        });
        formatCurrencyColumn(colInvSvcAcc, "white");

        colInvMachine.setCellValueFactory(cell -> {
            // Giả định: TotalAmount = TimeAmount + SvcAcc + SvcCash (Tùy logic DB)
            // Hoặc TotalAmount chỉ là tiền trừ tài khoản. 
            // Ở đây ta dùng công thức an toàn: Machine = Total - Services
            // Nếu model Invoice đã có getTimeAmount() thì dùng getter là tốt nhất.
            
            Invoice inv = cell.getValue();
            double totalSvc = (inv.getOrderItems() != null) ? 
                    inv.getOrderItems().stream().mapToDouble(OrderItem::getCost).sum() : 0;
            
            double machine = inv.getTotalAmount() - totalSvc;
            // Nếu logic hệ thống Invoice.Total chỉ lưu số tiền trừ TK, thì công thức sẽ khác.
            // Nhưng code cũ đang dùng logic này nên ta giữ nguyên.
            if (machine < 0) machine = 0; 
            
            return new SimpleDoubleProperty(machine).asObject();
        });
        formatCurrencyColumn(colInvMachine, "#34d399"); // Xanh lá (Máy)

        colInvTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        formatCurrencyColumn(colInvTotal, "#f472b6"); // Hồng (Tổng)

        tableInvoices.setItems(invoiceList);
    }

    private void setupOrderTable() {
        colOrdTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOrderedAt().format(dtf)));
        colOrdName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getServiceItem().getName()));
        colOrdQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colOrdPrice.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getServiceItem().getUnitPrice()));
        formatCurrencyColumn(colOrdPrice, "white");
        colOrdTotal.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getCost()));
        formatCurrencyColumn(colOrdTotal, "#fbbf24");

        // Cột Nguồn tiền
        colOrdSource.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPaymentSource().toString()));
        colOrdSource.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTextFill(Color.WHITE);
                } else {
                    setText(item);
                    if ("CASH".equals(item)) setTextFill(Color.web("#fbbf24")); // Vàng
                    else setTextFill(Color.web("#60a5fa")); // Xanh dương
                }
            }
        });

        // Cột Trạng thái
        colOrdStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));
        
        tableOrderHistory.setItems(orderList);
    }

    private void setupTopUpTable() {
        colTopId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTopTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCreatedAt().format(dtf)));
        colTopCustomer.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        colTopRole.setCellValueFactory(new PropertyValueFactory<>("operatorType"));
        colTopStaffId.setCellValueFactory(cell -> {
            Integer id = cell.getValue().getOperatorId();
            return new SimpleStringProperty((id == null || id == 0) ? "-" : String.valueOf(id));
        });
        colTopOperator.setCellValueFactory(new PropertyValueFactory<>("operatorName"));
        colTopAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        formatCurrencyColumn(colTopAmount, "#f472b6");
        colTopNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        tableTopUps.setItems(topUpList);
    }

    // ================== LOGIC LOAD DATA ==================
    @FXML private void onRefresh() {
        dpFrom.setValue(null); dpTo.setValue(null);
        loadData();
    }

    @FXML private void onFilter() { loadData(); }

    private void loadData() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        // --- 1. XỬ LÝ TIỀN NẠP (TOP UP) ---
        List<TopUpHistory> allTopUps = topUpRepo.findAll();
        List<TopUpHistory> filteredTopUps = allTopUps.stream()
            .filter(t -> isWithinDate(t.getCreatedAt().toLocalDate(), from, to))
            .collect(Collectors.toList());
        topUpList.setAll(filteredTopUps);

        double totalTopUp = filteredTopUps.stream().mapToDouble(TopUpHistory::getAmount).sum();

        // --- 2. XỬ LÝ HÓA ĐƠN & ORDER ---
        List<Invoice> allInvoices = invoiceRepo.findAll();
        List<Invoice> filteredInvoices = allInvoices.stream()
            .filter(i -> isWithinDate(i.getCreatedAt().toLocalDate(), from, to))
            .collect(Collectors.toList());
        invoiceList.setAll(filteredInvoices);

        double totalMachine = 0;
        double totalServiceCash = 0;
        double totalServiceAcc = 0;
        
        List<OrderItem> allOrderItems = new ArrayList<>();

        for (Invoice inv : filteredInvoices) {
            double invTotal = inv.getTotalAmount();
            double invSvcCash = 0;
            double invSvcAcc = 0;

            if (inv.getOrderItems() != null) {
                for (OrderItem item : inv.getOrderItems()) {
                    allOrderItems.add(item);
                    if (item.getPaymentSource() == PaymentSource.CASH) {
                        invSvcCash += item.getCost();
                    } else {
                        invSvcAcc += item.getCost();
                    }
                }
            }
            
            // Doanh thu máy = Tổng - Dịch vụ
            double invMachine = invTotal - (invSvcCash + invSvcAcc);
            if (invMachine < 0) invMachine = 0; // Fix trường hợp âm nếu logic sai

            totalMachine += invMachine;
            totalServiceCash += invSvcCash;
            totalServiceAcc += invSvcAcc;
        }
        orderList.setAll(allOrderItems);

        // --- 3. CẬP NHẬT GIAO DIỆN SỐ LIỆU ---

        // A. TỔNG THỰC THU (CASH FLOW) = TIỀN NẠP + ORDER TIỀN MẶT
        double totalRealCash = totalTopUp + totalServiceCash;
        lblTotalRealRevenue.setText(String.format("%,.0f VNĐ", totalRealCash));

        // B. TIỀN NẠP
        lblTotalTopUp.setText(String.format("%,.0f VNĐ", totalTopUp));

        // C. TIỀN MÁY (Trừ tài khoản)
        lblMachineRevenue.setText(String.format("%,.0f VNĐ", totalMachine));

        // D. TIỀN DỊCH VỤ (Tổng + Chi tiết)
        double totalService = totalServiceCash + totalServiceAcc;
        lblServiceRevenue.setText(String.format("%,.0f VNĐ", totalService));
        lblServiceDetail.setText(String.format("(TM: %,.0f - TK: %,.0f)", totalServiceCash, totalServiceAcc));
    }

    private boolean isWithinDate(LocalDate date, LocalDate from, LocalDate to) {
        return (from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to));
    }

    private <T> void formatCurrencyColumn(TableColumn<T, Double> col, String colorHex) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
                }
            }
        });
    }
}