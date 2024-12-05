package br.com.professorclaytonandrade.sistemaservicosjavafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class StartViewController {

    @FXML
    private void handleProdutos(ActionEvent event) {
        abrirTela("cadastro-produto.fxml", "Cadastro de Produtos");
    }

    @FXML
    private void handleIngredientes(ActionEvent event) {
        abrirTela("cadastro-ingrediente.fxml", "Cadastro de Ingredientes");
    }

    @FXML
    private void handleVendas(ActionEvent event) {
        abrirTela("registro-venda.fxml", "Registro de Vendas");
    }

    @FXML
    private void handleDespesas(ActionEvent event) {
        abrirTela("registro-despesa.fxml", "Controle de Despesas");
    }

    private void abrirTela(String fxmlFile, String titulo) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/br/com/professorclaytonandrade/sistemaservicosjavafx/" + fxmlFile));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
