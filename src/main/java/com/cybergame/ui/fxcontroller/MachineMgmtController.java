package com.cybergame.ui.fxcontroller;

import com.cybergame.controller.ComputerController;
import com.cybergame.model.entity.Computer;
import com.cybergame.model.enums.ComputerStatus;
import com.cybergame.repository.ComputerRepository;
import com.cybergame.repository.sql.ComputerRepositorySQL;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MachineMgmtController {

    @FXML private FlowPane machineGrid;
    @FXML private TextField txtSearch;

    // Toggle Group để reset bộ lọc nếu cần (Option)
    @FXML private ToggleButton btnAll;

    private final ComputerRepository repo = new ComputerRepositorySQL();
    private final ComputerController controller = new ComputerController(repo);

    private final List<Computer> allMachines = new ArrayList<>();

    private Computer selectedMachine;
    private VBox selectedCard;

    // ================= INIT =================
    @FXML
    public void initialize() {
        allMachines.addAll(repo.findAll());
        render(allMachines);
    }

    // ================= RENDER =================
    private void render(List<Computer> list) {
        machineGrid.getChildren().clear();
        // Reset selection khi render lại
        selectedMachine = null;
        selectedCard = null;

        for (Computer c : list) {
            machineGrid.getChildren().add(createCard(c));
        }
    }

    // TẠO CARD GIAO DIỆN ĐẸP THEO CSS MACHINE
    private VBox createCard(Computer c) {
        VBox card = new VBox();
        // Add class gốc cho card
        card.getStyleClass().add("machine-card");

        // 1. Add class màu sắc dựa theo status
        String statusStyleClass = "status-offline"; // Default
        switch (c.getStatus()) {
            case AVAILABLE -> statusStyleClass = "status-available";
            case IN_USE -> statusStyleClass = "status-in_use";
            case PAUSED -> statusStyleClass = "status-paused";
            case MAINTENANCE -> statusStyleClass = "status-maintenance";
            case OFFLINE -> statusStyleClass = "status-offline";
        }
        card.getStyleClass().add(statusStyleClass);

        // 2. Icon to (Giả lập icon bằng Emoji hoặc text, CSS sẽ phóng to nó)
        Label icon = new Label("🖥");
        icon.getStyleClass().add("big-icon");

        // 3. Tên máy
        Label name = new Label(c.getName());
        name.getStyleClass().add("machine-name");
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // 4. Trạng thái text
        Label statusLabel = new Label(c.getStatus().name());
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        // 5. Giá tiền
        Label price = new Label(String.format("%,.0f đ/h", c.getPricePerHour()));
        price.getStyleClass().add("info-label");

        // Ghép vào card
        card.getChildren().addAll(name, icon, statusLabel, price);

        // Sự kiện click
        card.setOnMouseClicked(e -> selectCard(card, c));

        return card;
    }

    // XỬ LÝ CHỌN (TOGGLE: CHỌN RỒI ẤN LẠI THÌ BỎ CHỌN)
    private void selectCard(VBox card, Computer c) {
        // Trường hợp 1: Click vào đúng cái đang chọn -> HỦY CHỌN
        if (selectedMachine != null && selectedMachine.equals(c)) {
            // Xóa hiệu ứng chọn (viền vàng/trắng gì đó tùy logic CSS custom hoặc set cứng)
            card.setStyle(""); 
            
            // Nếu muốn nó quay về style mặc định của class CSS thì chỉ cần xóa style inline
            // (Vì style inline đè lên class CSS)

            selectedMachine = null;
            selectedCard = null;
            return; 
        }

        // Trường hợp 2: Click vào cái mới -> Chọn cái mới
        if (selectedCard != null) {
            // Reset cái cũ
            selectedCard.setStyle("");
        }

        selectedCard = card;
        selectedMachine = c;

        // Highlight cái mới (Viền sáng màu trắng hoặc vàng để nổi bật trên nền tối)
        // Lưu ý: CSS machine-card đã có border màu status, ta đè border màu trắng để báo hiệu đang chọn
        card.setStyle("-fx-border-color: white; -fx-border-width: 3; -fx-background-color: #2c2d3b;");
    }

    // ================= FILTER =================
    @FXML private void filterAll() { render(allMachines); }
    @FXML private void filterAvailable() { filterByStatus(ComputerStatus.AVAILABLE); }
    @FXML private void filterInUse() { filterByStatus(ComputerStatus.IN_USE); }
    @FXML private void filterPaused() { filterByStatus(ComputerStatus.PAUSED); }
    @FXML private void filterMaintenance() { filterByStatus(ComputerStatus.MAINTENANCE); }
    @FXML private void filterOffline() { filterByStatus(ComputerStatus.OFFLINE); }

    private void filterByStatus(ComputerStatus status) {
        render(allMachines.stream().filter(c -> c.getStatus() == status).toList());
    }

    // ================= SEARCH =================
    @FXML
    private void handleSearch() {
        String key = txtSearch.getText().trim().toLowerCase();
        if (key.isEmpty()) {
            render(allMachines);
            // Nếu muốn khi xóa hết search thì tab "Tất cả" sáng lại thì xử lý thêm ở đây
            return;
        }

        render(allMachines.stream()
                .filter(c -> c.getName().toLowerCase().contains(key))
                .toList());
    }

    // ================= ADD =================
    @FXML
    private void handleAdd() {
        Dialog<Computer> dialog = new Dialog<>();
        dialog.setTitle("Thêm máy");

        ButtonType btnAdd = new ButtonType("Tạo", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        TextField txtName = new TextField();
        TextField txtPrice = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Tên máy:"), txtName);
        grid.addRow(1, new Label("Giá / giờ:"), txtPrice);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnAdd) {
                try {
                    Computer c = controller.createComputer(
                            txtName.getText(),
                            Double.parseDouble(txtPrice.getText())
                    );
                    c.setStatus(ComputerStatus.AVAILABLE);
                    return c;
                } catch (Exception e) {
                    alert("Lỗi nhập liệu: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            allMachines.add(c);
            render(allMachines);
        });
    }

    // ================= VIEW / EDIT =================
    @FXML
    private void handleView() {
        if (selectedMachine == null) {
            alert("Vui lòng chọn máy cần xem!");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thông tin: " + selectedMachine.getName());

        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        TextField txtName = new TextField(selectedMachine.getName());
        TextField txtPrice = new TextField(String.valueOf(selectedMachine.getPricePerHour()));

        ComboBox<ComputerStatus> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll(EnumSet.complementOf(EnumSet.of(ComputerStatus.IN_USE)));
        cbStatus.setValue(selectedMachine.getStatus());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Tên máy:"), txtName);
        grid.addRow(1, new Label("Giá / giờ:"), txtPrice);
        grid.addRow(2, new Label("Trạng thái:"), cbStatus);

        dialog.getDialogPane().setContent(grid);

        // NẾU ĐANG DÙNG -> CHỈ XEM
        if (selectedMachine.getStatus() == ComputerStatus.IN_USE) {
            txtName.setDisable(true);
            txtPrice.setDisable(true);
            cbStatus.setDisable(true);
            dialog.getDialogPane().lookupButton(btnSave).setDisable(true);
        }

        dialog.setResultConverter(btn -> {
            if (btn == btnSave) {
                selectedMachine.setName(txtName.getText());
                selectedMachine.setPricePerHour(Double.parseDouble(txtPrice.getText()));
                selectedMachine.setStatus(cbStatus.getValue());
                repo.save(selectedMachine);
                
                // Render lại để cập nhật màu sắc mới
                render(allMachines); 
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ================= DELETE =================
    @FXML
    private void handleDelete() {
        if (selectedMachine == null) {
            alert("Chưa chọn máy để xóa");
            return;
        }

        if (selectedMachine.getStatus() == ComputerStatus.IN_USE) {
            alert("Không thể xóa máy đang có khách chơi!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Xóa máy");
        confirm.setContentText("Bạn chắc chắn muốn xóa: " + selectedMachine.getName() + " ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                repo.delete(selectedMachine);
                allMachines.remove(selectedMachine);
                render(allMachines);
            }
        });
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show();
    }
}