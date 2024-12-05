package br.com.professorclaytonandrade.sistemaservicosjavafx.dao;

import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Despesa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {
    private final Connection connection;

    public DespesaDAO(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Despesa despesa) throws SQLException {
        String sql = "INSERT INTO despesa (descricao, valor, data) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherStatement(stmt, despesa);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    despesa.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Despesa> listarTodas() throws SQLException {
        List<Despesa> despesas = new ArrayList<>();
        String sql = "SELECT * FROM despesa ORDER BY data DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                despesas.add(criarDespesa(rs));
            }
        }
        return despesas;
    }

    public void atualizar(Despesa despesa) throws SQLException {
        String sql = "UPDATE despesa SET descricao = ?, valor = ?, data = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            preencherStatement(stmt, despesa);
            stmt.setInt(4, despesa.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM despesa WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private void preencherStatement(PreparedStatement stmt, Despesa despesa) throws SQLException {
        stmt.setString(1, despesa.getDescricao());
        stmt.setDouble(2, despesa.getValor());
        stmt.setDate(3, Date.valueOf(despesa.getData()));
    }

    private Despesa criarDespesa(ResultSet rs) throws SQLException {
        return new Despesa(
                rs.getInt("id"),
                rs.getString("descricao"),
                rs.getDouble("valor"),
                rs.getDate("data").toLocalDate()
        );
    }
}