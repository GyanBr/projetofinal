package br.com.professorclaytonandrade.sistemaservicosjavafx.config.versoesbasedados;

import br.com.professorclaytonandrade.sistemaservicosjavafx.config.conexao.FabricaDeConexao;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InicializadorBancoDados {
    private final Connection conexao; // Conexão com o banco de dados
    // Caminho onde os arquivos de migração estão localizados
    private static final String CAMINHO_MIGRACOES = "sql/"; // Define o caminho das migrações

    public InicializadorBancoDados() throws SQLException {
        // Construtor que obtém a conexão com o banco de dados ao criar a instância
        this.conexao = FabricaDeConexao.obterConexao();
    }

    public void inicializar() { // Método que inicia o processo de inicialização
        try {
            // Garante que a tabela de controle de versão existe
            criarSchemaVersaoTabela();

            // Lista e aplica os arquivos de migração
            List<Path> arquivosMigracao = listarArquivosDeMigracao(); // Obtém a lista de arquivos de migração

            // Percorre cada arquivo de migração
            for (Path caminho : arquivosMigracao) {
                // Extrai o número da versão a partir do nome do arquivo
                int idVersao = extrairVersaoDoArquivo(caminho.getFileName().toString());

                // Verifica se a migração já foi aplicada
                if (!isMigracaoAplicada(idVersao)) {
                    // Lê o conteúdo do arquivo SQL
                    String sql = lerArquivoSql(caminho);
                    // Aplica a migração no banco de dados
                    aplicarMigracao(idVersao, caminho.getFileName().toString(), sql);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace(); // Tratar exceções conforme necessário
        }
    }

    private List<Path> listarArquivosDeMigracao() throws IOException {
        try {
            // Obtém a URL da pasta de migrações
            URI uriMigracoes = getClass().getClassLoader().getResource(CAMINHO_MIGRACOES).toURI();
            // Converte a URL para um objeto Path
            Path diretorioMigracoes = Paths.get(uriMigracoes);

            // Lista os arquivos no diretório de migrações
            return Files.list(diretorioMigracoes)
                    .filter(Files::isRegularFile) // Filtra para obter apenas arquivos regulares
                    .sorted() // Ordena para garantir que as migrações sejam aplicadas na ordem correta
                    .collect(Collectors.toList()); // Coleta os resultados em uma lista
        } catch (URISyntaxException e) {
            throw new IOException("Erro ao acessar a pasta de migrações", e);
        } catch (NullPointerException e) {
            throw new IOException("Pasta de migrações não encontrada: " + CAMINHO_MIGRACOES); // Mensagem de erro caso a pasta não exista
        }
    }

    private String lerArquivoSql(Path caminhoArquivo) throws IOException, SQLException {
        // Cria um StringBuilder para armazenar o conteúdo do arquivo SQL
        StringBuilder sqlBuilder = new StringBuilder();
        // Lê o conteúdo do arquivo SQL linha por linha
        try (BufferedReader leitor = Files.newBufferedReader(caminhoArquivo)) {
            String linha;
            while ((linha = leitor.readLine()) != null) { // Continua lendo enquanto houver linhas
                sqlBuilder.append(linha).append("\n"); // Adiciona a linha ao StringBuilder
            }
        }
        String sql = sqlBuilder.toString().trim(); // Remove espaços em branco do início e fim

        // Verifica se a string SQL não está vazia
        if (sql.isEmpty()) {
            throw new SQLException("A string SQL não pode estar vazia: " + caminhoArquivo.toString()); // Mensagem de erro se estiver vazia
        }
        return sql; // Retorna o conteúdo SQL lido
    }

    private void aplicarMigracao(int idVersao, String descricao, String sql) throws SQLException {
        // Prepara a declaração SQL para a execução da migração
        try (PreparedStatement declaracao = conexao.prepareStatement(sql)) {
            declaracao.executeUpdate(); // Executa a atualização no banco de dados
            // Registra a migração aplicada no controle de versão
            registrarMigracao(idVersao, descricao);
        }
    }

    private void registrarMigracao(int idVersao, String descricao) throws SQLException {
        // SQL para registrar a migração na tabela de controle de versão
        String sql = "INSERT INTO schema_version (version_id, description) VALUES (?, ?)";
        try (PreparedStatement declaracao = conexao.prepareStatement(sql)) {
            declaracao.setInt(1, idVersao); // Define o ID da versão
            declaracao.setString(2, descricao); // Define a descrição da migração
            declaracao.executeUpdate(); // Executa a inserção na tabela
        }
    }

    private boolean isMigracaoAplicada(int idVersao) throws SQLException {
        // SQL para verificar se a migração foi aplicada
        String sql = "SELECT COUNT(*) FROM schema_version WHERE version_id = ?";
        try (PreparedStatement declaracao = conexao.prepareStatement(sql)) {
            declaracao.setInt(1, idVersao); // Define o ID da versão
            var resultSet = declaracao.executeQuery(); // Executa a consulta
            resultSet.next(); // Move para o próximo registro
            // Retorna true se a migração foi aplicada (contagem maior que 0)
            return resultSet.getInt(1) > 0;
        }
    }

    private void criarSchemaVersaoTabela() throws SQLException {
        // SQL para criar a tabela de controle de versão, se não existir
        String sql = "CREATE TABLE IF NOT EXISTS schema_version (" +
                "version_id INT PRIMARY KEY, " +
                "description VARCHAR(255), " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (PreparedStatement declaracao = conexao.prepareStatement(sql)) {
            declaracao.executeUpdate(); // Executa a criação da tabela
        }
    }

    private int extrairVersaoDoArquivo(String arquivo) {
        // Extrai o número da versão a partir do nome do arquivo (ex: V1__descricao.sql -> 1)
        String parteVersao = arquivo.split("__")[0].substring(1); // Remove o 'V' e pega o número
        return Integer.parseInt(parteVersao); // Retorna o número da versão como um inteiro
    }
}
