package com.cybergame.repository.sql;

import com.cybergame.model.entity.Computer;
import com.cybergame.model.enums.ComputerStatus;
import com.cybergame.repository.ComputerRepository;

import java.sql.*;
import java.util.*;

public class ComputerRepositorySQL implements ComputerRepository {

    @Override
    public void save(Computer c) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO computers
                (id, name, price_per_hour, status)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                status=VALUES(status)
             """)) {

            ps.setInt(1, c.getComputerId());
            ps.setString(2, c.getName());
            ps.setDouble(3, c.getPricePerHour());
            ps.setString(4, c.getStatus().name());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Computer> findAll() {
        List<Computer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM computers")) {

            while (rs.next()) {
                Computer c = new Computer(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price_per_hour")
                );
                if (ComputerStatus.valueOf(rs.getString("status")) == ComputerStatus.IN_USE) {
                    c.markInUse();
                }
                list.add(c);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void delete(Computer c) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                conn.prepareStatement("DELETE FROM computers WHERE id=?")) {

            ps.setInt(1, c.getComputerId());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
