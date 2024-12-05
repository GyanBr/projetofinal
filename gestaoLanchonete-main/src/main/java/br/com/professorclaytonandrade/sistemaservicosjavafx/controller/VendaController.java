package br.com.professorclaytonandrade.sistemaservicosjavafx.controller;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao.FabricaDeConexao;
import br.com.professorclaytonandrade.sistemaservicosjavafx.dao.ProdutoDAO;
import br.com.professorclaytonandrade.sistemaservicosjavafx.dao.VendaDAO;
import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Produto;
import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Venda;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class VendaController {
    @FXML private DatePicker dataPicker;
    @FXML private ComboBox<Produto> produtoComboBox;
    @FXML private TextField quantidadeTextField;
    @FXML private TextField precoTotalTextField;
    @FXML private TableView<Venda> vendasTableView;
    @FXML private TableColumn<Venda, LocalDate> dataColumn;
    @FXML private TableColumn<Venda, String> produtoColumn;
    @FXML private TableColumn<Venda, Integer> quantidadeColumn;
    @FXML private TableColumn<Venda, Double> valorColumn;

    private VendaDAO vendaDAO;
    private ProdutoDAO produtoDAO;
    private Connection conexao;
    private Venda vendaSelecionada;
    private ObservableList<Venda> listaVendas;

    @FXML
    public void initialize() {
        try {
            conexao = FabricaDeConexao.obterConexao();
            vendaDAO = new VendaDAO(conexao);
            produtoDAO = new ProdutoDAO(conexao);
            listaVendas = FXCollections.observableArrayList();
            vendasTableView.setItems(listaVendas);

            configurarColunas();
            configurarComboBox();
            configurarSelecaoTabela();
            configurarDataPicker();
            configurarCamposNumericos();
            configurarEventos();
            atualizarTabela();
        } catch (SQLException e) {
            mostrarErro("Erro ao inicializar", e.getMessage());
        }
    }

    private void configurarColunas() {
        dataColumn.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        produtoColumn.setCellValueFactory(cellData -> cellData.getValue().produtoDescricaoProperty());
        quantidadeColumn.setCellValueFactory(cellData -> cellData.getValue().quantidadeProperty().asObject());
        valorColumn.setCellValueFactory(cellData -> cellData.getValue().valorTotalProperty().asObject());

        valorColumn.setCellFactory(tc -> new TableCell<Venda, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", valor).replace(".", ","));
                }
            }
        });
    }

    private void configurarComboBox() throws SQLException {
        produtoComboBox.setItems(FXCollections.observableArrayList(produtoDAO.listarTodos()));
        produtoComboBox.setConverter(new javafx.util.StringConverter<Produto>() {
            @Override
            public String toString(Produto produto) {
                return produto != null ? produto.getDescricao() : "";
            }

            @Override
            public Produto fromString(String string) {
                return null;
            }
        });
    }

    private void configurarDataPicker() {
        dataPicker.setValue(LocalDate.now());
    }

    private void configurarCamposNumericos() {
        TextFormatter<String> quantidadeFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        });
        quantidadeTextField.setTextFormatter(quantidadeFormatter);
        quantidadeTextField.textProperty().addListener((obs, oldValue, newValue) -> calcularTotal());

        precoTotalTextField.setEditable(false);
    }

    private void configurarEventos() {
        produtoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> calcularTotal());
    }

    private void configurarSelecaoTabela() {
        vendasTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    vendaSelecionada = newSelection;
                    if (newSelection != null) {
                        preencherCampos(newSelection);
                    }
                });
    }

    private void calcularTotal() {
        Produto produtoSelecionado = produtoComboBox.getValue();
        if (produtoSelecionado != null && !quantidadeTextField.getText().isEmpty()) {
            try {
                int quantidade = Integer.parseInt(quantidadeTextField.getText());
                double total = produtoSelecionado.getPrecoVenda() * quantidade;
                precoTotalTextField.setText(String.format("%.2f", total).replace(".", ","));
            } catch (NumberFormatException e) {
                precoTotalTextField.clear();
            }
        }
    }

    @FXML
    private void handleSalvar() {
        try {
            if (!validarCampos()) return;

            Venda venda = criarVenda();
            if (vendaSelecionada != null) {
                venda.setId(vendaSelecionada.getId());
                vendaDAO.atualizar(venda);
                atualizarItemNaLista(venda);
            } else {
                vendaDAO.inserir(venda);
                listaVendas.add(venda);
            }

            limparSelecao();
            mostrarSucesso("Venda " + (vendaSelecionada != null ? "atualizada" : "registrada") + " com sucesso!");

        } catch (SQLException e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    private Venda criarVenda() {
        Produto produto = produtoComboBox.getValue();
        String valorTexto = precoTotalTextField.getText().trim().replace("R$", "").replace(" ", "");
        double valorTotal;
        try {
            valorTotal = Double.parseDouble(valorTexto.replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarErro("Erro de Formato", "Valor total inválido");
            throw e;
        }

        return new Venda(
                produto.getId(),
                produto.getDescricao(),
                Integer.parseInt(quantidadeTextField.getText().trim()),
                valorTotal,
                dataPicker.getValue()
        );
    }

    @FXML
    private void handleExcluir() {
        if (vendaSelecionada == null) {
            mostrarAlerta("Selecione uma venda para excluir.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacao(
                "Confirmar exclusão",
                "Deseja realmente excluir esta venda?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                vendaDAO.deletar(vendaSelecionada.getId());
                listaVendas.remove(vendaSelecionada);
                limparSelecao();
                mostrarSucesso("Venda excluída com sucesso!");
            } catch (SQLException e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpar() {
        limparSelecao();
    }

    private void atualizarItemNaLista(Venda venda) {
        int index = encontrarIndiceNaLista(venda.getId());
        if (index >= 0) {
            listaVendas.set(index, venda);
        }
    }

    private int encontrarIndiceNaLista(int id) {
        for (int i = 0; i < listaVendas.size(); i++) {
            if (listaVendas.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private boolean validarCampos() {
        if (dataPicker.getValue() == null) {
            mostrarAlerta("Selecione uma data.");
            return false;
        }

        if (produtoComboBox.getValue() == null) {
            mostrarAlerta("Selecione um produto.");
            return false;
        }

        try {
            int quantidade = Integer.parseInt(quantidadeTextField.getText().trim());
            if (quantidade <= 0) {
                mostrarAlerta("A quantidade deve ser maior que zero.");
                return false;
            }

            Produto produto = produtoComboBox.getValue();
            if (quantidade > produto.getQuantidadeEstoque()) {
                mostrarAlerta("Quantidade indisponível em estoque.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Quantidade inválida.");
            return false;
        }

        return true;
    }

    private void preencherCampos(Venda venda) {
        dataPicker.setValue(venda.getData());
        try {
            Produto produto = produtoDAO.buscarPorId(venda.getProdutoId());
            produtoComboBox.setValue(produto);
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar produto", e.getMessage());
        }
        quantidadeTextField.setText(String.valueOf(venda.getQuantidade()));
        precoTotalTextField.setText(String.format("%.2f", venda.getValorTotal()).replace(".", ","));
    }

    private void limparCampos() {
        dataPicker.setValue(LocalDate.now());
        produtoComboBox.setValue(null);
        quantidadeTextField.clear();
        precoTotalTextField.clear();
    }

    private void limparSelecao() {
        vendaSelecionada = null;
        limparCampos();
        vendasTableView.getSelectionModel().clearSelection();
    }

    private void atualizarTabela() throws SQLException {
        listaVendas.setAll(vendaDAO.listarTodas());
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