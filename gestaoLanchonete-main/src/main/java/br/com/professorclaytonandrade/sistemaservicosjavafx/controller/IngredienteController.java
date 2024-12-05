package br.com.professorclaytonandrade.sistemaservicosjavafx.controller;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao.FabricaDeConexao;
import br.com.professorclaytonandrade.sistemaservicosjavafx.dao.IngredienteDAO;
import br.com.professorclaytonandrade.sistemaservicosjavafx.model.Ingrediente;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class IngredienteController {

    @FXML private TextField nomeTextField;
    @FXML private TextField precoTextField;
    @FXML private TextField quantidadeTextField;
    @FXML private ComboBox<String> unidadeMedidaComboBox;
    @FXML private TableView<Ingrediente> ingredientesTableView;
    @FXML private TableColumn<Ingrediente, Integer> idColumn;
    @FXML private TableColumn<Ingrediente, String> nomeColumn;
    @FXML private TableColumn<Ingrediente, Double> quantidadeColumn;
    @FXML private TableColumn<Ingrediente, Double> precoColumn;
    @FXML private TableColumn<Ingrediente, String> unidadeColumn;

    private IngredienteDAO ingredienteDAO;
    private Connection conexao;
    private Ingrediente ingredienteSelecionado;

    @FXML
    public void initialize() {
        try {
            conexao = FabricaDeConexao.obterConexao();
            ingredienteDAO = new IngredienteDAO(conexao);

            configurarColunas();
            configurarComboBox();
            configurarSelecaoTabela();
            configurarCamposNumericos();
            atualizarTabela();

        } catch (SQLException e) {
            mostrarErro("Erro ao inicializar", e.getMessage());
        }
    }

    private void configurarColunas() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));
        quantidadeColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantidade()).asObject());
        precoColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPreco()).asObject());
        unidadeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnidadeMedida()));

        precoColumn.setCellFactory(tc -> new TableCell<Ingrediente, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", price));
                }
            }
        });

        quantidadeColumn.setCellFactory(tc -> new TableCell<Ingrediente, Double>() {
            @Override
            protected void updateItem(Double quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", quantity));
                }
            }
        });
    }

    private void configurarComboBox() {
        unidadeMedidaComboBox.setItems(FXCollections.observableArrayList(
                "Kg", "g", "L", "ml", "unidade"
        ));
    }

    private void configurarCamposNumericos() {
        precoTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                precoTextField.setText(oldValue);
            }
        });

        quantidadeTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                quantidadeTextField.setText(oldValue);
            }
        });
    }

    private void configurarSelecaoTabela() {
        ingredientesTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    ingredienteSelecionado = newSelection;
                    if (newSelection != null) {
                        preencherCampos(newSelection);
                    }
                });
    }

    @FXML
    private void handleSalvar() {
        try {
            if (!validarCampos()) return;

            Ingrediente ingrediente = criarIngrediente();
            if (ingredienteSelecionado != null) {
                ingrediente.setId(ingredienteSelecionado.getId());
                ingredienteDAO.atualizar(ingrediente);
            } else {
                ingredienteDAO.inserir(ingrediente);
            }

            atualizarTabela();
            limparSelecao();
            mostrarSucesso("Ingrediente " + (ingredienteSelecionado != null ? "atualizado" : "salvo") + " com sucesso!");

        } catch (SQLException e) {
            mostrarErro("Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void handleExcluir() {
        if (ingredienteSelecionado == null) {
            mostrarAlerta("Selecione um ingrediente para excluir.");
            return;
        }

        Optional<ButtonType> resultado = mostrarConfirmacao(
                "Confirmar exclusão",
                "Deseja realmente excluir o ingrediente " + ingredienteSelecionado.getNome() + "?"
        );

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                ingredienteDAO.deletar(ingredienteSelecionado.getId());
                atualizarTabela();
                limparSelecao();
                mostrarSucesso("Ingrediente excluído com sucesso!");
            } catch (SQLException e) {
                mostrarErro("Erro ao excluir", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLimpar() {
        limparSelecao();
    }

    private boolean validarCampos() {
        if (nomeTextField.getText().trim().isEmpty()) {
            mostrarAlerta("O nome é obrigatório.");
            return false;
        }

        try {
            double preco = Double.parseDouble(precoTextField.getText().trim());
            double quantidade = Double.parseDouble(quantidadeTextField.getText().trim());

            if (preco <= 0) {
                mostrarAlerta("O preço deve ser maior que zero.");
                return false;
            }
            if (quantidade <= 0) {
                mostrarAlerta("A quantidade deve ser maior que zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Preço e quantidade devem ser números válidos.");
            return false;
        }

        if (unidadeMedidaComboBox.getValue() == null) {
            mostrarAlerta("Selecione uma unidade de medida.");
            return false;
        }

        return true;
    }

    private Ingrediente criarIngrediente() {
        return new Ingrediente(
                nomeTextField.getText().trim(),
                Double.parseDouble(quantidadeTextField.getText().trim()),
                Double.parseDouble(precoTextField.getText().trim()),
                unidadeMedidaComboBox.getValue()
        );
    }

    private void preencherCampos(Ingrediente ingrediente) {
        nomeTextField.setText(ingrediente.getNome());
        precoTextField.setText(String.valueOf(ingrediente.getPreco()));
        quantidadeTextField.setText(String.valueOf(ingrediente.getQuantidade()));
        unidadeMedidaComboBox.setValue(ingrediente.getUnidadeMedida());
    }

    private void limparCampos() {
        nomeTextField.clear();
        precoTextField.clear();
        quantidadeTextField.clear();
        unidadeMedidaComboBox.setValue(null);
    }

    private void limparSelecao() {
        ingredienteSelecionado = null;
        limparCampos();
        ingredientesTableView.getSelectionModel().clearSelection();
    }

    private void atualizarTabela() throws SQLException {
        ingredientesTableView.setItems(FXCollections.observableArrayList(
                ingredienteDAO.listarTodos()
        ));
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