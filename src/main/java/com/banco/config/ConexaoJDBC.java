package com.banco.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoJDBC {
    private static final String URL = "jdbc:h2:./data/banco";

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, "sa", "");
    }
}