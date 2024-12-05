package br.com.professorclaytonandrade.sistemaservicosjavafx.controller;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao.FabricaDeConexao;
import br.com.professorclaytonandrade.sistemaservicosjavafx.dao.ProdutoDAO;
import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class ProdutoController {
    @FXML private TextField descricaoField;
    @FXML private TextField precoField;
    @FXML private TextField quantidadeField;
    @FXML private TextField markupField;
    @FXML private TableView<Produto> produtosTable;
    @FXML private TableColumn<Produto, Integer> idColumn;
    @FXML private TableColumn<Produto, String> descricaoColumn;
    @FXML private TableColumn<Produto, Double> precoColumn;
    @FXML private TableColumn<Produto, Integer> quantidadeColumn;
    @FXML private TableColumn<Produto, Double> markupColumn;

    private ProdutoDAO produtoDAO;
    private Connection conexao;
    private Produto produtoSelecionado;
    private ObservableList<Produto> listaProdutos;

    @FXML
    public void initialize() {
        try {
            conexao = FabricaDeConexao.obterConexao();
            produtoDAO = new ProdutoDAO(conexao);
            listaProdutos = FXCollections.observableArrayList();
            produtosTable.setItems(listaProdutos);

            configurarColunas();
            configurarSelecaoTabela();
            configurarCamposNumericos();
            atualizarTabela();
        } catch (SQLException e) {
            mostrarErro("Erro ao inicializar", e.getMessage());
        }
    }

    private void configurarColunas() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        descricaoColumn.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        precoColumn.setCellValueFactory(cellData -> cellData.getValue().precoProperty().asObject());
        quantidadeColumn.setCellValueFactory(cellData -> cellData.getValue().quantidadeEstoqueProperty().asObject());
        markupColumn.setCellValueFactory(cellData -> cellData.getValue().markupProperty().asObject());

        precoColumn.setCellFactory(tc -> new TableCell<Produto, Double>() {
            @Override
            protected void updateItem(Double preco, boolean empty) {
                super.updateItem(preco, empty);
                setText(empty ? null : String.format("R$ %.2f", preco));
            }
        });

        markupColumn.setCellFactory(tc -> new TableCell<Produto, Double>() {
            @Override
            protected void updateItem(Double markup, boolean empty) {
                super.updateItem(markup, empty);
                setText(empty ? null : String.format("%.1f%%", markup));
            }
        });
    }

    private void configurarCamposNumericos() {
        precoField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                precoField.setText(oldValue);
            }
        });

        quantidadeField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantidadeField.setText(oldValue);
            }
        });

        markupField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                markupField.setText(oldValue);
            }
        });
    }

    private void configurarSelecaoTabela() {
        produtosTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    produtoSelecionado = newSelection;
                    if (newSelection != null) {
                        preencherCampos(newSelection);
                    }
                });
    }

    @FXML
    private void handleSalvar() {
        try {
            if (!validarCampos()) return;

            Produto produto = criarProduto();
            if (produtoSelecionado != null) {
                produto.setId(produtoSelecionado.getId());
                produtoDAO.atualizar(produto);
                atualizarItemNaLista(produto);
            } else {
                produtoDAO.inserir(produto);
                listaProdutos.add(produto);
            }

            limparSelecao();
            mostrarSucesso("Produto " + (produtoSelecionado != null ? "atualizado" : "salvo") + " com sucesso!");

        } catch (SQLException e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void handleExcluir() {
        if (produtoSelecionado == null) {
            mostrarAlerta("Selecione um produto para excluir.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacao(
                "Confirmar exclusão",
                "Deseja realmente excluir o produto " + produtoSelecionado.getDescricao() + "?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                produtoDAO.deletar(produtoSelecionado.getId());
                listaProdutos.remove(produtoSelecionado);
                limparSelecao();
                mostrarSucesso("Produto excluído com sucesso!");
            } catch (SQLException e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpar() {
        limparSelecao();
    }

    private void atualizarItemNaLista(Produto produto) {
        int index = encontrarIndiceNaLista(produto.getId());
        if (index >= 0) {
            listaProdutos.set(index, produto);
        }
    }

    private int encontrarIndiceNaLista(int id) {
        for (int i = 0; i < listaProdutos.size(); i++) {
            if (listaProdutos.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private boolean validarCampos() {
        if (descricaoField.getText().trim().isEmpty()) {
            mostrarAlerta("A descrição é obrigatória.");
            return false;
        }

        try {
            double preco = Double.parseDouble(precoField.getText().trim());
            int quantidade = Integer.parseInt(quantidadeField.getText().trim());
            double markup = Double.parseDouble(markupField.getText().trim());

            if (preco <= 0) {
                mostrarAlerta("O preço deve ser maior que zero.");
                return false;
            }
            if (quantidade < 0) {
                mostrarAlerta("A quantidade não pode ser negativa.");
                return false;
            }
            if (markup < 0) {
                mostrarAlerta("O markup não pode ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Valores numéricos inválidos.");
            return false;
        }

        return true;
    }

    private Produto criarProduto() {
        return new Produto(
                descricaoField.getText().trim(),
                Double.parseDouble(precoField.getText().trim()),
                Integer.parseInt(quantidadeField.getText().trim()),
                Double.parseDouble(markupField.getText().trim())
        );
    }

    private void preencherCampos(Produto produto) {
        descricaoField.setText(produto.getDescricao());
        precoField.setText(String.format("%.2f", produto.getPreco()));
        quantidadeField.setText(String.valueOf(produto.getQuantidadeEstoque()));
        markupField.setText(String.format("%.2f", produto.getMarkup()));
    }

    private void limparCampos() {
        descricaoField.clear();
        precoField.clear();
        quantidadeField.clear();
        markupField.clear();
    }

    private void limparSelecao() {
        produtoSelecionado = null;
        limparCampos();
        produtosTable.getSelectionModel().clearSelection();
    }

    private void atualizarTabela() throws SQLException {
        listaProdutos.setAll(produtoDAO.listarTodos());
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private Optional<ButtonType> mostrarConfirmacao(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensagem);
        return alert.showAndWait();
    }
}