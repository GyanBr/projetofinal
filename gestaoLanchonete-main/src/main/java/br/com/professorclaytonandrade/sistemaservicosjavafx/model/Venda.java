package br.com.professorclaytonandrade.sistemaservicosjavafx.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Venda {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty produtoId = new SimpleIntegerProperty();
    private final StringProperty produtoDescricao = new SimpleStringProperty();
    private final IntegerProperty quantidade = new SimpleIntegerProperty();
    private final DoubleProperty valorTotal = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> data = new SimpleObjectProperty<>();

    public Venda(int produtoId, String produtoDescricao, int quantidade, double valorTotal, LocalDate data) {
        setProdutoId(produtoId);
        setProdutoDescricao(produtoDescricao);
        setQuantidade(quantidade);
        setValorTotal(valorTotal);
        setData(data);
    }

    public Venda(int id, int produtoId, String produtoDescricao, int quantidade, double valorTotal, LocalDate data) {
        setId(id);
        setProdutoId(produtoId);
        setProdutoDescricao(produtoDescricao);
        setQuantidade(quantidade);
        setValorTotal(valorTotal);
        setData(data);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public int getProdutoId() { return produtoId.get(); }
    public void setProdutoId(int produtoId) { this.produtoId.set(produtoId); }
    public IntegerProperty produtoIdProperty() { return produtoId; }

    public String getProdutoDescricao() { return produtoDescricao.get(); }
    public void setProdutoDescricao(String descricao) { this.produtoDescricao.set(descricao); }
    public StringProperty produtoDescricaoProperty() { return produtoDescricao; }

    public int getQuantidade() { return quantidade.get(); }
    public void setQuantidade(int quantidade) { this.quantidade.set(quantidade); }
    public IntegerProperty quantidadeProperty() { return quantidade; }

    public double getValorTotal() { return valorTotal.get(); }
    public void setValorTotal(double valor) { this.valorTotal.set(valor); }
    public DoubleProperty valorTotalProperty() { return valorTotal; }

    public LocalDate getData() { return data.get(); }
    public void setData(LocalDate data) { this.data.set(data); }
    public ObjectProperty<LocalDate> dataProperty() { return data; }
}