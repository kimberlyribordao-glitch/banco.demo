package com.banco.repository;

import com.banco.config.ConexaoJDBC;
import com.banco.model.TipoTransacao;
import com.banco.model.Transacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoRepository {

    public void registrar(Transacao t) {
        String sql = "INSERT INTO transacao (tipo, valor, data_hora, conta_id) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexaoJDBC.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, t.getTipo().name());
            ps.setDouble(2, t.getValor());
            ps.setTimestamp(3, Timestamp.valueOf(t.getDataHora()));
            ps.setLong(4, t.getConta().getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar transação: " + e.getMessage(), e);
        }
    }

    public List<Transacao> listarPorConta(Long contaId) {
        String sql = "SELECT id, tipo, valor, data_hora FROM transacao " +
                "WHERE conta_id = ? ORDER BY data_hora, id";
        List<Transacao> extrato = new ArrayList<>();

        try (Connection con = ConexaoJDBC.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, contaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    extrato.add(new Transacao(
                            rs.getLong("id"),
                            TipoTransacao.valueOf(rs.getString("tipo")),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar extrato: " + e.getMessage(), e);
        }
        return extrato;
    }

    public List<Transacao> listarPorPeriodo(Long contaId, LocalDateTime inicio, LocalDateTime fimExclusivo) {
        String sql = "SELECT id, tipo, valor, data_hora FROM transacao " +
                     "WHERE conta_id = ? AND data_hora >= ? AND data_hora < ? " +
                     "ORDER BY data_hora, id";
        List<Transacao> extrato = new ArrayList<>();

        try (Connection con = ConexaoJDBC.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, contaId);
            ps.setTimestamp(2, Timestamp.valueOf(inicio));
            ps.setTimestamp(3, Timestamp.valueOf(fimExclusivo));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    extrato.add(new Transacao(
                            rs.getLong("id"),
                            TipoTransacao.valueOf(rs.getString("tipo")),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar extrato por período: " + e.getMessage(), e);
        }
        return extrato;
    }
}
