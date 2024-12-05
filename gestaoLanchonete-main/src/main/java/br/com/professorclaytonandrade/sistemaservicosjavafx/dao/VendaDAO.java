package br.com.professorclaytonandrade.sistemaservicosjavafx.dao;

import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Venda;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {
    private final Connection connection;

    public VendaDAO(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Venda venda) throws SQLException {
        String sql = "INSERT INTO venda (produto_id, quantidade, valor_total, data) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherStatement(stmt, venda);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    venda.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<Venda> listarTodas() throws SQLException {
        List<Venda> vendas = new ArrayList<>();
        String sql = "SELECT v.*, p.descricao as produto_descricao FROM venda v " +
                "INNER JOIN produto p ON v.produto_id = p.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vendas.add(criarVenda(rs));
            }
        }
        return vendas;
    }

    public void atualizar(Venda venda) throws SQLException {
        String sql = "UPDATE venda SET produto_id = ?, quantidade = ?, valor_total = ?, data = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            preencherStatement(stmt, venda);
            stmt.setInt(5, venda.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM venda WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private void preencherStatement(PreparedStatement stmt, Venda venda) throws SQLException {
        stmt.setInt(1, venda.getProdutoId());
        stmt.setInt(2, venda.getQuantidade());
        stmt.setDouble(3, venda.getValorTotal());
        stmt.setDate(4, Date.valueOf(venda.getData()));
    }

    private Venda criarVenda(ResultSet rs) throws SQLException {
        return new Venda(
                rs.getInt("id"),
                rs.getInt("produto_id"),
                rs.getString("produto_descricao"),
                rs.getInt("quantidade"),
                rs.getDouble("valor_total"),
                rs.getDate("data").toLocalDate()
        );
    }
}