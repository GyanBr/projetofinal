package br.com.professorclaytonandrade.sistemaservicosjavafx;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.versoesbasedados.InicializadorBancoDados;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class StartApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent parent = FXMLLoader.load(StartApplication.class.getResource("start-view.fxml"));
        Scene scene = new Scene(parent);
        stage.setMaximized(true);
        stage.setTitle("Sistema de Gerenciamento");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws SQLException {
        // Inicializa o banco de dados e aplica as migrações, se houver
        InicializadorBancoDados inicializadorBancoDados = new InicializadorBancoDados();
        inicializadorBancoDados.inicializar();

        // Iniciar a aplicação JavaFX
        launch();
    }
}