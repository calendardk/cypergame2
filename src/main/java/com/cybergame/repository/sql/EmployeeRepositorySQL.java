package com.cybergame.repository.sql;

import com.cybergame.model.entity.Employee;
import com.cybergame.repository.EmployeeRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepositorySQL implements EmployeeRepository {

    @Override
    public void save(Employee e) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO employees
                (id, username, password, display_name, phone, locked)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    display_name = VALUES(display_name),
                    phone        = VALUES(phone),
                    locked       = VALUES(locked)
             """)) {

            ps.setInt(1, e.getUserId());
            ps.setString(2, e.getUsername());
            ps.setString(3, e.getPasswordHash());
            ps.setString(4, e.getDisplayName());
            ps.setString(5, e.getPhone());
            ps.setBoolean(6, e.isLocked());

            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Employee findByUsername(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "SELECT * FROM employees WHERE username=?")) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Employee e = new Employee(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("display_name"),
                    rs.getString("phone")
            );

            // 🔒 map locked
            if (rs.getBoolean("locked")) {
                e.lock();
            }

            return e;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Employee> findAll() {

        List<Employee> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {

                Employee e = new Employee(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("display_name"),
                        rs.getString("phone")
                );

                // 🔒 map locked
                if (rs.getBoolean("locked")) {
                    e.lock();
                }

                list.add(e);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return list;
    }

    @Override
    public void delete(Employee e) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "DELETE FROM employees WHERE id=?")) {

            ps.setInt(1, e.getUserId());
            ps.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
