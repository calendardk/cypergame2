package com.cybergame.repository.sql;

import com.cybergame.model.entity.Invoice;
import com.cybergame.model.entity.OrderItem;
import com.cybergame.repository.InvoiceRepository;
import com.cybergame.util.SerializationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceRepositorySQL implements InvoiceRepository {

    @Override
    public void save(Invoice i) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO invoices
                (id, account_id, account_name, computer_name,
                 created_at,
                 time_amount,
                 service_amount,
                 service_account_amount,
                 service_cash_amount,
                 total,
                 order_items)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
             """)) {

            ps.setInt(1, i.getInvoiceId());
            ps.setInt(2, i.getAccountId());
            ps.setString(3, i.getAccountName());   // snapshot username
            ps.setString(4, i.getComputerName());
            ps.setTimestamp(5, Timestamp.valueOf(i.getCreatedAt()));

            ps.setDouble(6, i.getTimeAmount());
            ps.setDouble(7, i.getServiceAmount());
            ps.setDouble(8, i.getServiceAccountAmount());
            ps.setDouble(9, i.getServiceCashAmount());

            ps.setDouble(10, i.getTotalAmount());

            // serialize order items (snapshot)
            ps.setString(11,
                    SerializationUtil.serialize(i.getOrderItems())
            );

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Invoice> findAll() {

        List<Invoice> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM invoices")) {

            while (rs.next()) {

                // 🚫 KHÔNG TÍNH TOÁN – LOAD THUẦN DB
                Invoice inv = new Invoice(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getString("account_name"),
                        rs.getString("computer_name"),
                        rs.getTimestamp("created_at").toLocalDateTime(),

                        rs.getDouble("time_amount"),
                        rs.getDouble("service_amount"),
                        rs.getDouble("service_account_amount"),
                        rs.getDouble("service_cash_amount"),
                        rs.getDouble("total")
                );

                // deserialize order_items (nếu có)
                String data = rs.getString("order_items");
                if (data != null && !data.isEmpty()) {
                    List<OrderItem> orders =
                            SerializationUtil.deserialize(data);
                    inv.getOrderItems().addAll(orders);
                }

                list.add(inv);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public void delete(Invoice i) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM invoices WHERE id=?")) {

            ps.setInt(1, i.getInvoiceId());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
