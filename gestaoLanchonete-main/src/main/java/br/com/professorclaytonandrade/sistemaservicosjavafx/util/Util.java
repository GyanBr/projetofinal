package br.com.professorclaytonandrade.sistemaservicosjavafx.util;

import br.com.professorclaytonandrade.sistemaservicosjavafx.StartApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Util {

    public static void janelaModal(AnchorPane anchorPane, String telaFXML, String tituloJanela) throws IOException {
        Parent parent = carregarFXML(telaFXML);
        Stage popupStage = criarPopupStage(tituloJanela, parent, anchorPane);
        configurarJanela(popupStage);
        exibirJanelaModal(popupStage);
    }

    public static <T> boolean janelaModalComDados(AnchorPane anchorPane, String telaFXML, String tituloJanela, T objeto, Object controller) throws IOException {
        // Carregar o arquivo FXML
        FXMLLoader loader = new FXMLLoader(StartApplication.class.getResource(telaFXML));
        Parent parent = loader.load();
        // Obter o controlador do loader
        Object loadedController = loader.getController();
        // Verificar se o controlador passado é do tipo correto
        if (controller != null && loadedController != null && controller.getClass() == loadedController.getClass()) {
            // Supondo que o controlador tem um método setDado
            try {
                // Aqui usamos a classe do objeto para encontrar o método setDado
                Method setDadoMethod = loadedController.getClass().getMethod("setDado", objeto.getClass());
                setDadoMethod.invoke(loadedController, objeto);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao invocar o método setDado no controlador.");
            }
        } else {
            throw new IllegalArgumentException("O controlador não corresponde ao esperado ou não pôde ser carregado.");
        }
        // Criar e exibir a janela modal
        Stage popupStage = criarPopupStage(tituloJanela, parent, anchorPane);
        configurarJanela(popupStage);
        exibirJanelaModal(popupStage);
        return true;
    }

    // Carrega o arquivo FXML fornecido
    private static Parent carregarFXML(String telaFXML) throws IOException {
        return FXMLLoader.load(StartApplication.class.getResource(telaFXML));
    }

    // Cria o Stage (janela) para o popup
    private static Stage criarPopupStage(String tituloJanela, Parent parent, AnchorPane anchorPane) {
        Stage popupStage = new Stage();
        popupStage.setTitle(tituloJanela);
        Scene scene = new Scene(parent);
        popupStage.setScene(scene);
        popupStage.initOwner(anchorPane.getScene().getWindow());
        return popupStage;
    }

    // Configura a janela, incluindo modal, centralização e redimensionamento
    private static void configurarJanela(Stage popupStage) {
        popupStage.initModality(Modality.APPLICATION_MODAL); // Mudança aqui para APPLICATION_MODAL
        popupStage.centerOnScreen();
        popupStage.setResizable(false);
    }

    // Exibe o Stage como um modal
    private static void exibirJanelaModal(Stage popupStage) {
        popupStage.showAndWait(); // Já está correto
    }

    public static boolean exibirAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        // Mostrar o alerta e esperar a resposta do usuário
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}
