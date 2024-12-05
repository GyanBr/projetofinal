package br.com.professorclaytonandrade.sistemaservicosjavafx.dao;

import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Produto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {
    private final Connection conexao;

    public ProdutoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (descricao, preco, quantidade_estoque, markup) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherStatement(stmt, produto);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET descricao = ?, preco = ?, quantidade_estoque = ?, markup = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            preencherStatement(stmt, produto);
            stmt.setInt(5, produto.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Produto> listarTodos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto";
        try (Statement stmt = conexao.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                produtos.add(criarProduto(rs));
            }
        }
        return produtos;
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarProduto(rs);
                }
            }
        }
        return null;
    }

    private void preencherStatement(PreparedStatement stmt, Produto produto) throws SQLException {
        stmt.setString(1, produto.getDescricao());
        stmt.setDouble(2, produto.getPreco());
        stmt.setInt(3, produto.getQuantidadeEstoque());
        stmt.setDouble(4, produto.getMarkup());
    }

    private Produto criarProduto(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("id"),
                rs.getString("descricao"),
                rs.getDouble("preco"),
                rs.getInt("quantidade_estoque"),
                rs.getDouble("markup")
        );
    }
}