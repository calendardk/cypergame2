package com.cybergame.repository.sql;

import com.cybergame.model.entity.Account;
import com.cybergame.repository.AccountRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepositorySQL implements AccountRepository {

    @Override
    public void save(Account acc) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO accounts
                (id, username, password, display_name, phone, balance, locked, vip)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    balance = VALUES(balance),
                    locked  = VALUES(locked),
                    vip     = VALUES(vip),
                    phone   = VALUES(phone)
             """)) {

            ps.setInt(1, acc.getUserId());
            ps.setString(2, acc.getUsername());
            ps.setString(3, acc.getPasswordHash());
            ps.setString(4, acc.getDisplayName());
            ps.setString(5, acc.getPhone());
            ps.setDouble(6, acc.getBalance());
            ps.setBoolean(7, acc.isLocked());
            ps.setBoolean(8, acc.getTimeDiscountRate() < 1);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Account findByUsername(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "SELECT * FROM accounts WHERE username=?")) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Account acc = new Account(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("display_name"),
                    rs.getString("phone"),
                    rs.getBoolean("vip")
            );

            acc.topUp(rs.getDouble("balance"));
            if (rs.getBoolean("locked")) acc.lock();

            return acc;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Account> findAll() {

        List<Account> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM accounts")) {

            while (rs.next()) {

                Account acc = new Account(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("display_name"),
                        rs.getString("phone"),
                        rs.getBoolean("vip")
                );

                acc.topUp(rs.getDouble("balance"));
                if (rs.getBoolean("locked")) acc.lock();

                list.add(acc);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public void delete(Account acc) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM accounts WHERE id=?")) {

            ps.setInt(1, acc.getUserId());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
