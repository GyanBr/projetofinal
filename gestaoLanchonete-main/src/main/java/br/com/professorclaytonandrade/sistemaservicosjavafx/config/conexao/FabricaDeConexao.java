package br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class FabricaDeConexao {

    private static HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(FabricaDeConexao.class);

    // Configuração do pool de conexões
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/lanchonete");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(10); // Máximo de conexões simultâneas
        config.setMinimumIdle(2); // Número mínimo de conexões ociosas
        config.setIdleTimeout(30000); // Tempo ocioso antes de fechar uma conexão
        config.setMaxLifetime(1800000); // Tempo máximo de vida para cada conexão

        dataSource = new HikariDataSource(config);
    }

    // Método para obter uma conexão
    public static Connection obterConexao() throws SQLException {
        logger.info("Obtendo conexão do pool...");
        return dataSource.getConnection();
    }

    // Método para fechar o pool de conexões
    public static void fecharDataSource() {
        if (dataSource != null) {
            logger.info("Fechando o pool de conexões...");
            dataSource.close();
        }
    }

    public static void main(String[] args) {
        try (Connection conexao = FabricaDeConexao.obterConexao()) {
            // Lógica para manipulação do banco de dados usando 'conexao'
            logger.info("Conexão obtida com sucesso!");
        } catch (SQLException e) {
            logger.error("Erro ao obter a conexão: {}", e.getMessage());
        }
    }
}
