package br.com.professorclaytonandrade.sistemaservicosjavafx.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Despesa {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty descricao = new SimpleStringProperty();
    private final DoubleProperty valor = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> data = new SimpleObjectProperty<>();

    public Despesa(String descricao, double valor, LocalDate data) {
        setDescricao(descricao);
        setValor(valor);
        setData(data);
    }

    public Despesa(int id, String descricao, double valor, LocalDate data) {
        this(descricao, valor, data);
        setId(id);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getDescricao() { return descricao.get(); }
    public void setDescricao(String descricao) { this.descricao.set(descricao); }
    public StringProperty descricaoProperty() { return descricao; }

    public double getValor() { return valor.get(); }
    public void setValor(double valor) { this.valor.set(valor); }
    public DoubleProperty valorProperty() { return valor; }

    public LocalDate getData() { return data.get(); }
    public void setData(LocalDate data) { this.data.set(data); }
    public ObjectProperty<LocalDate> dataProperty() { return data; }
}