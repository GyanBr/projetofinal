package br.com.professorclaytonandrade.sistemaservicosjavafx.model;

public class Ingrediente {
    private int id;
    private String nome;
    private double quantidade;
    private double preco;
    private String unidadeMedida;

    public Ingrediente(int id, String nome, double quantidade, double preco, String unidadeMedida) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
        this.unidadeMedida = unidadeMedida;
    }

    public Ingrediente(String nome, double quantidade, double preco, String unidadeMedida) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
        this.unidadeMedida = unidadeMedida;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
}