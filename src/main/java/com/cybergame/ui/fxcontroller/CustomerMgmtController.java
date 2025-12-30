package com.cybergame.ui.fxcontroller;

import com.cybergame.context.AccountContext;
import com.cybergame.controller.AccountController;
import com.cybergame.controller.TopUpController;
import com.cybergame.model.entity.Account;
import com.cybergame.repository.sql.AccountRepositorySQL;
import com.cybergame.repository.sql.TopUpHistoryRepositorySQL;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomerMgmtController implements Initializable {

    // --- FXML ELEMENTS ---
    @FXML private TableView<Account> tableAccounts;
    @FXML private TextField txtSearch;
    
    @FXML private TableColumn<Account, Integer> colId;
    @FXML private TableColumn<Account, String> colUsername;
    @FXML private TableColumn<Account, String> colFullname;
    @FXML private TableColumn<Account, String> colPhone;
    @FXML private TableColumn<Account, Double> colBalance;
    @FXML private TableColumn<Account, String> colStatus;

    // --- DEPENDENCIES ---
    private final AccountRepositorySQL accRepo = new AccountRepositorySQL();
    private final AccountController accCtrl = new AccountController(accRepo);
    private final TopUpController topUpCtrl = new TopUpController(accRepo, new TopUpHistoryRepositorySQL());

    private ObservableList<Account> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
        setupSearch();
        setupStyles();
    }

    private void setupStyles() {
        // Cấu hình giao diện tối màu
        tableAccounts.setStyle("-fx-control-inner-background: #0d1b2a; -fx-base: #0d1b2a; -fx-background-color: #0d1b2a;");

        // Row Factory: Xử lý click và tô màu
        tableAccounts.setRowFactory(tv -> {
            TableRow<Account> row = new TableRow<>();
            
            // Click đúp hoặc click lại để bỏ chọn
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (!row.isEmpty() && row.isSelected() && e.getButton() == MouseButton.PRIMARY) {
                    tableAccounts.getSelectionModel().clearSelection();
                    e.consume();
                }
            });

            // Listener tô màu
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!row.isEmpty()) updateRowStyle(row, isSelected);
            });
            
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) row.setStyle("-fx-background-color: transparent;");
                else updateRowStyle(row, row.isSelected());
            });
            
            return row;
        });
        
        Platform.runLater(() -> tableAccounts.getSelectionModel().clearSelection());
    }

    private void updateRowStyle(TableRow<Account> row, boolean isSelected) {
        if (isSelected) {
            row.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            row.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullname.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Hiển thị tiền: Format số đẹp
        colBalance.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getBalance()).asObject());
        colBalance.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.format("%,.0f đ", item));
                if (!empty) setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            }
        });

        // Hiển thị trạng thái: Check Online từ Singleton Context
        colStatus.setCellValueFactory(cell -> {
            boolean isOnline = AccountContext.getInstance().isOnline(cell.getValue().getUsername());
            
            String status;
            if (cell.getValue().isLocked()) status = "🚫 BỊ KHÓA";
            else if (isOnline) status = "🟢 Online";
            else status = "⚪ Offline";
            
            return new SimpleStringProperty(status);
        });
        
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                if (!empty && item != null) {
                    if (item.contains("BỊ KHÓA")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else if (item.contains("Online")) setStyle("-fx-text-fill: #00d2d3; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-text-fill: gray; -fx-alignment: CENTER;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    // 🔥 LOAD DATA: KẾT HỢP DB VÀ RAM (CONTEXT)
    private void loadData() {
        List<Account> dbList = accRepo.findAll(); // Lấy tất cả từ két sắt (DB)
        List<Account> displayList = new ArrayList<>();
        
        // Lấy cái túi tiền chung ra
        AccountContext context = AccountContext.getInstance();

        for (Account accDB : dbList) {
            // Kiểm tra xem thằng này có đang chơi (Online) không
            Account accOnline = context.get(accDB.getUsername());
            
            if (accOnline != null) {
                // Nếu Online: Dùng object từ RAM (để thấy tiền đang bị trừ)
                displayList.add(accOnline); 
            } else {
                // Nếu Offline: Dùng object từ DB
                displayList.add(accDB);
            }
        }
        
        masterData.setAll(displayList);
        tableAccounts.refresh();
    }

    private void setupSearch() {
        FilteredList<Account> filteredData = new FilteredList<>(masterData, p -> true);
        
        txtSearch.textProperty().addListener((obs, oldVal, newValue) -> {
            filteredData.setPredicate(acc -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return acc.getUsername().toLowerCase().contains(lower)
                    || acc.getDisplayName().toLowerCase().contains(lower)
                    || (acc.getPhone() != null && acc.getPhone().contains(lower));
            });
        });

        SortedList<Account> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableAccounts.comparatorProperty());
        tableAccounts.setItems(sortedData);
    }

    // ================== ACTIONS ==================

    @FXML private void onAdd() { showAccountDialog(null); }

    @FXML private void onUpdate() {
        Account selected = tableAccounts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn khách hàng cần sửa!");
            return;
        }
        // Lấy object chuẩn (nếu đang online thì lấy từ RAM) để sửa
        showAccountDialog(getRealAccount(selected));
    }

    @FXML private void onLockUnlock() {
        Account selected = tableAccounts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Account target = getRealAccount(selected);

        if (target.isLocked()) {
            accCtrl.unlock(target);
            showAlert("Mở khóa", "Đã MỞ KHÓA: " + target.getUsername());
        } else {
            accCtrl.lock(target);
            // Lưu ý: Nếu user đang online mà bị khóa, SessionManager cần có logic check lock để kick user ra.
            showAlert("Khóa", "Đã KHÓA: " + target.getUsername());
        }
        tableAccounts.refresh(); 
    }

    @FXML private void onDelete() {
        Account selected = tableAccounts.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // 🔥 CHECK CONTEXT: Cấm xóa khi đang Online
        if (AccountContext.getInstance().isOnline(selected.getUsername())) {
            showAlert("Cấm Xóa", "Khách hàng đang Online! Vui lòng tắt máy trạm trước.");
            return;
        }

        if (selected.getBalance() > 0) {
            showAlert("Cảnh báo", "Tài khoản còn tiền (" + String.format("%,.0f đ", selected.getBalance()) + "). Không thể xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản: " + selected.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            accCtrl.delete(selected);
            masterData.remove(selected);
            tableAccounts.getSelectionModel().clearSelection();
        }
    }

    @FXML private void onTopUp() {
        Account selected = tableAccounts.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn khách hàng để nạp tiền!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("10000");
        dialog.setTitle("Nạp Tiền");
        dialog.setHeaderText("Nạp tiền cho: " + selected.getUsername());
        dialog.setContentText("Nhập số tiền:");

        dialog.showAndWait().ifPresent(str -> {
            try {
                double amount = Double.parseDouble(str);
                if (amount > 0) {
                    // 🔥 QUAN TRỌNG: Nạp vào object trong Context (nếu có)
                    Account target = getRealAccount(selected);
                    
                    topUpCtrl.topUp(target, "ADMIN", null, "Manager", amount, "Admin TopUp");
                    
                    tableAccounts.refresh();
                    showAlert("Thành công", "Đã nạp thêm " + String.format("%,.0f đ", amount));
                }
            } catch (NumberFormatException e) {
                showAlert("Lỗi", "Số tiền không hợp lệ!");
            }
        });
    }

    @FXML private void onRefresh() {
        loadData(); // Tự động đồng bộ lại từ Context
        setupSearch(); 
        tableAccounts.getSelectionModel().clearSelection();
    }

    // --- HELPER: Lấy Account chuẩn (Ưu tiên RAM nếu đang Online) ---
    private Account getRealAccount(Account selected) {
        Account onlineAcc = AccountContext.getInstance().get(selected.getUsername());
        return (onlineAcc != null) ? onlineAcc : selected;
    }

    // ================== DIALOG FORM ==================
    private void showAccountDialog(Account existingAccount) {
        Dialog<Account> dialog = new Dialog<>();
        dialog.setTitle(existingAccount == null ? "Thêm Mới" : "Cập Nhật");
        dialog.setHeaderText(existingAccount == null ? "Nhập thông tin tài khoản mới" : "Sửa thông tin: " + existingAccount.getUsername());

        ButtonType btnTypeSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTypeSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtUser = new TextField(); txtUser.setPromptText("Username");
        TextField txtName = new TextField(); txtName.setPromptText("Họ tên");
        TextField txtPhone = new TextField(); txtPhone.setPromptText("SĐT");

        PasswordField txtPassHidden = new PasswordField(); txtPassHidden.setPromptText("Mật khẩu");
        TextField txtPassVisible = new TextField(); txtPassVisible.setPromptText("Mật khẩu");
        txtPassVisible.setManaged(false); txtPassVisible.setVisible(false);
        txtPassHidden.textProperty().bindBidirectional(txtPassVisible.textProperty());

        CheckBox chkShowPass = new CheckBox("Hiện pass");
        chkShowPass.selectedProperty().addListener((obs, o, n) -> {
            txtPassVisible.setManaged(n); txtPassVisible.setVisible(n);
            txtPassHidden.setManaged(!n); txtPassHidden.setVisible(!n);
        });
        VBox passContainer = new VBox(5, txtPassHidden, txtPassVisible, chkShowPass);

        if (existingAccount != null) {
            txtUser.setText(existingAccount.getUsername());
            txtUser.setDisable(true);
            txtPassHidden.setText(existingAccount.getPasswordHash());
            txtName.setText(existingAccount.getDisplayName());
            txtPhone.setText(existingAccount.getPhone());
        }

        grid.add(new Label("User:"), 0, 0); grid.add(txtUser, 1, 0);
        grid.add(new Label("Pass:"), 0, 1); grid.add(passContainer, 1, 1);
        grid.add(new Label("Tên:"), 0, 2);   grid.add(txtName, 1, 2);
        grid.add(new Label("SĐT:"), 0, 3);      grid.add(txtPhone, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnTypeSave) {
                return new Account(0, txtUser.getText(), txtPassHidden.getText(), txtName.getText(), txtPhone.getText(), false);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(form -> {
            if (form.getUsername().isEmpty() || form.getPasswordHash().isEmpty()) {
                showAlert("Lỗi", "Thiếu thông tin User/Pass!");
                return;
            }
            try {
                if (existingAccount == null) {
                    Account newAcc = accCtrl.createAccount(form.getUsername(), form.getPasswordHash(), form.getDisplayName(), form.getPhone(), false);
                    masterData.add(newAcc);
                } else {
                    existingAccount.setPasswordHash(form.getPasswordHash());
                    existingAccount.setDisplayName(form.getDisplayName());
                    existingAccount.setPhone(form.getPhone());
                    accRepo.save(existingAccount); // Save DB
                }
                tableAccounts.refresh();
            } catch (Exception e) {
                showAlert("Lỗi", "Không thể lưu (Trùng user?): " + e.getMessage());
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}