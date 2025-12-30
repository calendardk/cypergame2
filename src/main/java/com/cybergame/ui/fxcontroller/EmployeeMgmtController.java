package com.cybergame.ui.fxcontroller;

import com.cybergame.controller.EmployeeController;
import com.cybergame.model.entity.Employee;
import com.cybergame.repository.EmployeeRepository;
import com.cybergame.repository.sql.EmployeeRepositorySQL;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.Optional;

public class EmployeeMgmtController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colStatus; // [MỚI] Cột trạng thái
    
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbStatusFilter; // [MỚI] Bộ lọc

    private final EmployeeRepository repo = new EmployeeRepositorySQL();
    private final EmployeeController controller = new EmployeeController(repo);
    private final ObservableList<Employee> data = FXCollections.observableArrayList();

    // ================= INIT =================
    @FXML
    public void initialize() {
        // Setup bộ lọc
        cmbStatusFilter.setItems(FXCollections.observableArrayList("Tất cả", "Hoạt động", "Đã khóa"));
        cmbStatusFilter.setValue("Tất cả");
        
        // Khi chọn bộ lọc -> chạy lại hàm search
        cmbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        // Setup cột
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // [MỚI] Setup cột trạng thái
        colStatus.setCellValueFactory(cellData -> {
            boolean locked = cellData.getValue().isLocked();
            return new javafx.beans.property.SimpleStringProperty(locked ? "Đã khóa" : "Hoạt động");
        });

        // [MỚI] Tô màu chữ trạng thái
        colStatus.setCellFactory(new Callback<TableColumn<Employee, String>, TableCell<Employee, String>>() {
            @Override
            public TableCell<Employee, String> call(TableColumn<Employee, String> param) {
                return new TableCell<Employee, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setTextFill(Color.WHITE); // Default
                        } else {
                            setText(item);
                            if ("Đã khóa".equals(item)) {
                                setTextFill(Color.RED);
                                setStyle("-fx-font-weight: bold;");
                            } else {
                                setTextFill(Color.LIGHTGREEN);
                                setStyle("-fx-font-weight: normal;");
                            }
                        }
                    }
                };
            }
        });

        // Load dữ liệu ban đầu
        handleSearch(); 
        employeeTable.setItems(data);

        // Style bảng (Giữ nguyên code cũ của bạn)
        employeeTable.setStyle(
                "-fx-control-inner-background: #0d1b2a;" +
                "-fx-base: #0d1b2a;" +
                "-fx-background-color: #0d1b2a;"
        );

        employeeTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();

            row.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (!row.isEmpty() && row.isSelected() && e.getButton() == MouseButton.PRIMARY) {
                    employeeTable.getSelectionModel().clearSelection();
                    e.consume();
                }
            });

            row.selectedProperty().addListener((obs, oldSel, isSelected) -> {
                if (!row.isEmpty()) updateRowStyle(row, isSelected);
            });

            row.itemProperty().addListener((obs, o, n) -> {
                if (n == null) {
                    row.setStyle("-fx-background-color: transparent;");
                } else {
                    updateRowStyle(row, row.isSelected());
                }
            });

            return row;
        });

        Platform.runLater(() -> employeeTable.getSelectionModel().clearSelection());
    }

    private void updateRowStyle(TableRow<Employee> row, boolean isSelected) {
        if (isSelected) {
            row.setStyle("-fx-background-color: #008000; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            row.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        }
    }

    // ================= SEARCH & FILTER =================
    @FXML
    private void handleSearch() {
        String key = txtSearch.getText().trim().toLowerCase();
        String statusFilter = cmbStatusFilter.getValue();
        
        data.clear();

        for (Employee e : repo.findAll()) {
            // 1. Kiểm tra từ khóa (Tên hoặc SĐT)
            boolean matchKey = key.isEmpty() || 
                               e.getDisplayName().toLowerCase().contains(key) || 
                               e.getPhone().contains(key);
            
            // 2. Kiểm tra trạng thái
            boolean matchStatus = true;
            if ("Hoạt động".equals(statusFilter)) {
                matchStatus = !e.isLocked();
            } else if ("Đã khóa".equals(statusFilter)) {
                matchStatus = e.isLocked();
            }

            // Nếu thỏa mãn cả 2 thì thêm vào danh sách hiển thị
            if (matchKey && matchStatus) {
                data.add(e);
            }
        }
    }

    // ================= ADD =================
    @FXML
    private void handleAddEmployee() {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Thêm nhân viên");

        ButtonType btnAdd = new ButtonType("Tạo", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtUser = new TextField();

        TextField txtPassVisible = new TextField();
        PasswordField txtPassHidden = new PasswordField();

        CheckBox chkHide = new CheckBox("Ẩn mật khẩu");
        chkHide.setSelected(true);

        txtPassVisible.setVisible(false);
        txtPassVisible.setManaged(false);

        chkHide.selectedProperty().addListener((obs, o, hide) -> {
            if (hide) {
                txtPassHidden.setText(txtPassVisible.getText());
                txtPassHidden.setVisible(true);
                txtPassHidden.setManaged(true);
                txtPassVisible.setVisible(false);
                txtPassVisible.setManaged(false);
            } else {
                txtPassVisible.setText(txtPassHidden.getText());
                txtPassVisible.setVisible(true);
                txtPassVisible.setManaged(true);
                txtPassHidden.setVisible(false);
                txtPassHidden.setManaged(false);
            }
        });

        VBox passBox = new VBox(5, txtPassHidden, txtPassVisible, chkHide);

        TextField txtName = new TextField();
        TextField txtPhone = new TextField();

        grid.addRow(0, new Label("Username:"), txtUser);
        grid.addRow(1, new Label("Password:"), passBox);
        grid.addRow(2, new Label("Họ tên:"), txtName);
        grid.addRow(3, new Label("SĐT:"), txtPhone);

        dialog.getDialogPane().setContent(grid);

        Node btnCreateNode = dialog.getDialogPane().lookupButton(btnAdd);
        btnCreateNode.addEventFilter(ActionEvent.ACTION, e -> {
            String pass = chkHide.isSelected() ? txtPassHidden.getText() : txtPassVisible.getText();
            if (txtUser.getText().isBlank() || pass.isBlank()
                    || txtName.getText().isBlank() || txtPhone.getText().isBlank()) {
                showAlert("Vui lòng điền đầy đủ thông tin!");
                e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == btnAdd) {
                String pass = chkHide.isSelected() ? txtPassHidden.getText() : txtPassVisible.getText();
                return controller.createEmployee(
                        txtUser.getText(),
                        pass,
                        txtName.getText(),
                        txtPhone.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(e -> handleSearch()); // Refresh list sau khi thêm
    }

    // ================= VIEW / EDIT =================
    @FXML
    private void handleViewEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn nhân viên");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thông tin nhân viên");

        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtUser = new TextField(selected.getUsername());
        txtUser.setDisable(true);

        TextField txtPassVisible = new TextField(selected.getPasswordHash());
        PasswordField txtPassHidden = new PasswordField();
        txtPassHidden.setText(selected.getPasswordHash());

        CheckBox chkHide = new CheckBox("Ẩn mật khẩu");
        chkHide.setSelected(true);

        txtPassVisible.setVisible(false);
        txtPassVisible.setManaged(false);

        chkHide.selectedProperty().addListener((obs, o, hide) -> {
            if (hide) {
                txtPassHidden.setText(txtPassVisible.getText());
                txtPassHidden.setVisible(true);
                txtPassHidden.setManaged(true);
                txtPassVisible.setVisible(false);
                txtPassVisible.setManaged(false);
            } else {
                txtPassVisible.setText(txtPassHidden.getText());
                txtPassVisible.setVisible(true);
                txtPassVisible.setManaged(true);
                txtPassHidden.setVisible(false);
                txtPassHidden.setManaged(false);
            }
        });

        VBox passBox = new VBox(5, txtPassHidden, txtPassVisible, chkHide);

        TextField txtName = new TextField(selected.getDisplayName());
        TextField txtPhone = new TextField(selected.getPhone());

        grid.addRow(0, new Label("Username:"), txtUser);
        grid.addRow(1, new Label("Password:"), passBox);
        grid.addRow(2, new Label("Họ tên:"), txtName);
        grid.addRow(3, new Label("SĐT:"), txtPhone);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                String pass = chkHide.isSelected() ? txtPassHidden.getText() : txtPassVisible.getText();
                selected.setPasswordHash(pass);
                selected.setDisplayName(txtName.getText());
                selected.setPhone(txtPhone.getText());
                repo.save(selected);
                employeeTable.refresh();
            }
            return null;
        });

        dialog.showAndWait();
    }
    
    // ================= LOCK / UNLOCK [MỚI] =================
    @FXML
    private void handleLockUnlock() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Vui lòng chọn nhân viên để thao tác!");
            return;
        }
        
        // Gọi controller để xử lý logic khóa/mở
        if (selected.isLocked()) {
            controller.unlock(selected);
            showAlert("Đã MỞ KHÓA tài khoản: " + selected.getUsername());
        } else {
            controller.lock(selected);
            showAlert("Đã KHÓA tài khoản: " + selected.getUsername());
        }
        
        // Refresh bảng để cập nhật màu sắc
        employeeTable.refresh();
    }

    // ================= DELETE =================
    @FXML
    private void handleDeleteEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn nhân viên");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xác nhận xóa");
        confirm.setContentText("Xóa nhân viên: " + selected.getDisplayName() + " ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                controller.delete(selected);
                data.remove(selected);
                employeeTable.getSelectionModel().clearSelection();
            }
        });
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}