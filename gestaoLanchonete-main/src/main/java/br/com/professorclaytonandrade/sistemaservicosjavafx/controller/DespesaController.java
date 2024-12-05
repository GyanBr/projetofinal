package br.com.professorclaytonandrade.sistemaservicosjavafx.controller;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao.FabricaDeConexao;
import br.com.professorclaytonandrade.sistemaservicosjavafx.dao.DespesaDAO;
import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Despesa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class DespesaController {
    @FXML private TextArea descricaoTextArea;
    @FXML private TextField valorTextField;
    @FXML private DatePicker dataPicker;
    @FXML private TableView<Despesa> despesasTableView;
    @FXML private TableColumn<Despesa, LocalDate> dataColumn;
    @FXML private TableColumn<Despesa, String> descricaoColumn;
    @FXML private TableColumn<Despesa, Double> valorColumn;

    private DespesaDAO despesaDAO;
    private Connection conexao;
    private Despesa despesaSelecionada;
    private ObservableList<Despesa> listaDespesas;

    @FXML
    public void initialize() {
        try {
            conexao = FabricaDeConexao.obterConexao();
            despesaDAO = new DespesaDAO(conexao);
            listaDespesas = FXCollections.observableArrayList();
            despesasTableView.setItems(listaDespesas);

            configurarColunas();
            configurarSelecaoTabela();
            configurarDataPicker();
            configurarCamposNumericos();
            atualizarTabela();

        } catch (SQLException e) {
            mostrarErro("Erro ao inicializar", e.getMessage());
        }
    }

    private void configurarColunas() {
        dataColumn.setCellValueFactory(cellData -> cellData.getValue().dataProperty());
        descricaoColumn.setCellValueFactory(cellData -> cellData.getValue().descricaoProperty());
        valorColumn.setCellValueFactory(cellData -> cellData.getValue().valorProperty().asObject());

        valorColumn.setCellFactory(tc -> new TableCell<Despesa, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty ? null : String.format("R$ %.2f", valor));
            }
        });
    }

    private void configurarDataPicker() {
        dataPicker.setValue(LocalDate.now());
    }

    private void configurarCamposNumericos() {
        valorTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                valorTextField.setText(oldValue);
            }
        });
    }

    private void configurarSelecaoTabela() {
        despesasTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    despesaSelecionada = newSelection;
                    if (newSelection != null) {
                        preencherCampos(newSelection);
                    }
                });
    }

    @FXML
    private void handleSalvar() {
        try {
            if (!validarCampos()) return;

            Despesa despesa = criarDespesa();
            if (despesaSelecionada != null) {
                despesa.setId(despesaSelecionada.getId());
                despesaDAO.atualizar(despesa);
                atualizarItemNaLista(despesa);
            } else {
                despesaDAO.inserir(despesa);
                listaDespesas.add(despesa);
            }

            limparSelecao();
            mostrarSucesso("Despesa " + (despesaSelecionada != null ? "atualizada" : "registrada") + " com sucesso!");

        } catch (SQLException e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void handleExcluir() {
        if (despesaSelecionada == null) {
            mostrarAlerta("Selecione uma despesa para excluir.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacao(
                "Confirmar exclusão",
                "Deseja realmente excluir esta despesa?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                despesaDAO.deletar(despesaSelecionada.getId());
                listaDespesas.remove(despesaSelecionada);
                limparSelecao();
                mostrarSucesso("Despesa excluída com sucesso!");
            } catch (SQLException e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpar() {
        limparSelecao();
    }

    private void atualizarItemNaLista(Despesa despesa) {
        int index = encontrarIndiceNaLista(despesa.getId());
        if (index >= 0) {
            listaDespesas.set(index, despesa);
        }
    }

    private int encontrarIndiceNaLista(int id) {
        for (int i = 0; i < listaDespesas.size(); i++) {
            if (listaDespesas.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private boolean validarCampos() {
        if (descricaoTextArea.getText().trim().isEmpty()) {
            mostrarAlerta("A descrição é obrigatória.");
            return false;
        }

        if (dataPicker.getValue() == null) {
            mostrarAlerta("Selecione uma data.");
            return false;
        }

        try {
            double valor = Double.parseDouble(valorTextField.getText().trim());
            if (valor <= 0) {
                mostrarAlerta("O valor deve ser maior que zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Valor inválido.");
            return false;
        }

        return true;
    }

    private Despesa criarDespesa() {
        return new Despesa(
                descricaoTextArea.getText().trim(),
                Double.parseDouble(valorTextField.getText().trim()),
                dataPicker.getValue()
        );
    }

    private void preencherCampos(Despesa despesa) {
        descricaoTextArea.setText(despesa.getDescricao());
        valorTextField.setText(String.format("%.2f", despesa.getValor()));
        dataPicker.setValue(despesa.getData());
    }

    private void limparCampos() {
        descricaoTextArea.clear();
        valorTextField.clear();
        dataPicker.setValue(LocalDate.now());
    }

    private void limparSelecao() {
        despesaSelecionada = null;
        limparCampos();
        despesasTableView.getSelectionModel().clearSelection();
    }

    private void atualizarTabela() throws SQLException {
        listaDespesas.setAll(despesaDAO.listarTodas());
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