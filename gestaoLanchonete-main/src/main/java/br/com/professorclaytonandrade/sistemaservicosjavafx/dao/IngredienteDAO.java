package br.com.professorclaytonandrade.sistemaservicosjavafx.dao;

import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Ingrediente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredienteDAO {
    private final Connection connection;

    public IngredienteDAO(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Ingrediente ingrediente) throws SQLException {
        String sql = "INSERT INTO ingrediente (nome, quantidade, preco, unidade_medida) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, ingrediente.getNome());
            stmt.setDouble(2, ingrediente.getQuantidade());
            stmt.setDouble(3, ingrediente.getPreco());
            stmt.setString(4, ingrediente.getUnidadeMedida());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                ingrediente.setId(rs.getInt(1));
            }
        }
    }

    public List<Ingrediente> listarTodos() throws SQLException {
        List<Ingrediente> ingredientes = new ArrayList<>();
        String sql = "SELECT * FROM ingrediente";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ingredientes.add(new Ingrediente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("quantidade"),
                        rs.getDouble("preco"),
                        rs.getString("unidade_medida")
                ));
            }
        }
        return ingredientes;
    }

    public void atualizar(Ingrediente ingrediente) throws SQLException {
        String sql = "UPDATE ingrediente SET nome = ?, quantidade = ?, preco = ?, unidade_medida = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ingrediente.getNome());
            stmt.setDouble(2, ingrediente.getQuantidade());
            stmt.setDouble(3, ingrediente.getPreco());
            stmt.setString(4, ingrediente.getUnidadeMedida());
            stmt.setInt(5, ingrediente.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM ingrediente WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}