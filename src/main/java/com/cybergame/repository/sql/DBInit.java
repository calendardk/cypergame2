package com.cybergame.repository.sql;

import java.sql.Connection;
import java.sql.Statement;

public class DBInit {

    public static void init() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {

            // ================= ACCOUNTS =================
            st.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE,
                password VARCHAR(100),
                display_name VARCHAR(100),
                phone VARCHAR(20),
                balance DOUBLE,
                locked BOOLEAN DEFAULT FALSE,
                vip BOOLEAN
            )
            """);

            // ================= EMPLOYEES =================
            st.execute("""
            CREATE TABLE IF NOT EXISTS employees (
                id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE,
                password VARCHAR(100),
                display_name VARCHAR(100),
                phone VARCHAR(20),
                locked BOOLEAN DEFAULT FALSE
            )
            """);

            // ================= COMPUTERS =================
            st.execute("""
            CREATE TABLE IF NOT EXISTS computers (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(50),
                price_per_hour DOUBLE,
                status VARCHAR(20)
            )
            """);

            // ================= SERVICES =================
            // 🔥 THÊM CATEGORY (enum ServiceCategory)
            st.execute("""
            CREATE TABLE IF NOT EXISTS services (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(50),
                price DOUBLE,
                category VARCHAR(30),
                locked BOOLEAN DEFAULT FALSE
            )
            """);

            // ================= INVOICES =================
            // 🔥 SNAPSHOT CHUẨN (CÁCH 2)
            st.execute("""
            CREATE TABLE IF NOT EXISTS invoices (
                id INT PRIMARY KEY AUTO_INCREMENT,
                account_id INT,
                account_name VARCHAR(50),
                computer_name VARCHAR(50),
                created_at DATETIME,

                time_amount DOUBLE,
                service_amount DOUBLE,
                service_account_amount DOUBLE,
                service_cash_amount DOUBLE,

                total DOUBLE,
                order_items TEXT
            )
            """);

            // ================= TOPUP HISTORY =================
            st.execute("""
            CREATE TABLE IF NOT EXISTS topup_history (
                id INT PRIMARY KEY AUTO_INCREMENT,

                account_id INT NOT NULL,
                account_name VARCHAR(50),

                operator_type VARCHAR(20) NOT NULL, -- ADMIN | EMPLOYEE
                operator_id INT NULL,
                operator_name VARCHAR(100) NOT NULL,

                amount DOUBLE NOT NULL,
                created_at DATETIME,
                note VARCHAR(255)
            )
            """);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
