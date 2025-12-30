package com.cybergame.ui.fxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ManagerController {

    @FXML
    private VBox sidebar;

    @FXML
    private StackPane contentArea;

    private boolean sidebarVisible = true;

    /* ====== LOAD MẶC ĐỊNH ====== */
    @FXML
    public void initialize() {
        showOverview();
    }

    /* ====== SIDEBAR ====== */
    @FXML
    private void handleToggleMenu() {
        sidebarVisible = !sidebarVisible;
        sidebar.setManaged(sidebarVisible);
        sidebar.setVisible(sidebarVisible);
    }

    @FXML
    private void handleLogout() {
        // để trống hoặc quay về login sau
    }

    /* ====== ĐIỀU HƯỚNG ====== */
    @FXML
    private void showOverview() {
        loadView("/fxml/admin/admin_overview.fxml");
    }

    @FXML
    private void showMachineMgmt() {
        loadView("/fxml/admin/machine_mgmt.fxml");
    }

    @FXML
    private void showEmployeeMgmt() {
        loadView("/fxml/admin/employee_mgmt.fxml");
    }

    @FXML
    private void showCustomerMgmt() {
        loadView("/fxml/admin/customer_mgmt.fxml");
    }

    @FXML
    private void showSessionMgmt() {
        loadView("/fxml/admin/session_mgmt.fxml");
    }

    @FXML
    private void showProductMgmt() {
        loadView("/fxml/admin/product_mgmt.fxml");
    }

    @FXML
    private void showRevenueReport() {
        loadView("/fxml/admin/revenue_report.fxml");
    }

    /* ====== CORE LOADER ====== */
    private void loadView(String path) {
        try {
            Parent view = FXMLLoader.load(
                    getClass().getResource(path)
            );
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
