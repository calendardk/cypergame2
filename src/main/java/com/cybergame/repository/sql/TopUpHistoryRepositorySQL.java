package com.cybergame.repository.sql;

import com.cybergame.model.entity.TopUpHistory;
import com.cybergame.repository.TopUpHistoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TopUpHistoryRepositorySQL implements TopUpHistoryRepository {

    @Override
    public void save(TopUpHistory h) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO topup_history
                (account_id, account_name,
                 operator_type, operator_id, operator_name,
                 amount, created_at, note)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
             """)) {

            ps.setInt(1, h.getAccountId());
            ps.setString(2, h.getAccountName());
            ps.setString(3, h.getOperatorType());

            if (h.getOperatorId() != null) {
                ps.setInt(4, h.getOperatorId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setString(5, h.getOperatorName());
            ps.setDouble(6, h.getAmount());
            ps.setTimestamp(7, Timestamp.valueOf(h.getCreatedAt()));
            ps.setString(8, h.getNote());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
public void delete(TopUpHistory h) {
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps =
                 conn.prepareStatement(
                         "DELETE FROM topup_history WHERE id=?")) {

        ps.setInt(1, h.getId());
        ps.executeUpdate();

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}


    @Override
    public List<TopUpHistory> findAll() {
        List<TopUpHistory> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs =
                     st.executeQuery("SELECT * FROM topup_history ORDER BY created_at DESC")) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public List<TopUpHistory> findByAccount(int accountId) {
        List<TopUpHistory> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(
                             "SELECT * FROM topup_history WHERE account_id=? ORDER BY created_at DESC")) {

            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    // ===== MAP =====
    private TopUpHistory map(ResultSet rs) throws SQLException {
        TopUpHistory h = new TopUpHistory();

        h.setId(rs.getInt("id"));
        h.setAccountId(rs.getInt("account_id"));
        h.setAccountName(rs.getString("account_name"));
        h.setOperatorType(rs.getString("operator_type"));

        int opId = rs.getInt("operator_id");
        h.setOperatorId(rs.wasNull() ? null : opId);

        h.setOperatorName(rs.getString("operator_name"));
        h.setAmount(rs.getDouble("amount"));
        h.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        h.setNote(rs.getString("note"));

        return h;
    }
}
