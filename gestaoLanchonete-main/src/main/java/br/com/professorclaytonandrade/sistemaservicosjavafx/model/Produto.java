package br.com.professorclaytonandrade.sistemaservicosjavafx.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Produto {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty descricao = new SimpleStringProperty();
    private final DoubleProperty preco = new SimpleDoubleProperty();
    private final IntegerProperty quantidadeEstoque = new SimpleIntegerProperty();
    private final DoubleProperty markup = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> dataCadastro = new SimpleObjectProperty<>();

    public Produto() {
        this.dataCadastro.set(LocalDate.now());
    }

    public Produto(String descricao, double preco, int quantidadeEstoque, double markup) {
        this();
        setDescricao(descricao);
        setPreco(preco);
        setQuantidadeEstoque(quantidadeEstoque);
        setMarkup(markup);
    }

    public Produto(int id, String descricao, double preco, int quantidadeEstoque, double markup) {
        this(descricao, preco, quantidadeEstoque, markup);
        setId(id);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getDescricao() { return descricao.get(); }
    public void setDescricao(String descricao) { this.descricao.set(descricao); }
    public StringProperty descricaoProperty() { return descricao; }

    public double getPreco() { return preco.get(); }
    public void setPreco(double preco) { this.preco.set(preco); }
    public DoubleProperty precoProperty() { return preco; }

    public int getQuantidadeEstoque() { return quantidadeEstoque.get(); }
    public void setQuantidadeEstoque(int quantidade) { this.quantidadeEstoque.set(quantidade); }
    public IntegerProperty quantidadeEstoqueProperty() { return quantidadeEstoque; }

    public double getMarkup() { return markup.get(); }
    public void setMarkup(double markup) { this.markup.set(markup); }
    public DoubleProperty markupProperty() { return markup; }

    public LocalDate getDataCadastro() { return dataCadastro.get(); }
    public void setDataCadastro(LocalDate data) { this.dataCadastro.set(data); }
    public ObjectProperty<LocalDate> dataCadastroProperty() { return dataCadastro; }

    public double getPrecoVenda() {
        return getPreco() * (1 + getMarkup() / 100);
    }
}