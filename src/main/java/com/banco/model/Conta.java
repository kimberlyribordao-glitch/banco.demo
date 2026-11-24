package com.banco.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private double saldo;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transacao> transacoes = new ArrayList<>();

    public Conta() {}

    public Conta(String numero, Cliente cliente) {
        this.numero = numero;
        this.cliente = cliente;
    }

    public void depositar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor inválido.");
        saldo += valor;
    }

    // Cada tipo de conta implementa sua regra: POLIMORFISMO
    public abstract void sacar(double valor);

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public double getSaldo() { return saldo; }
    protected void setSaldo(double saldo) { this.saldo = saldo; }
    public Cliente getCliente() { return cliente; }
    public List<Transacao> getTransacoes() { return transacoes; }

    // Por padrão a conta não rende. A poupança sobrescreve este método.
    public double calcularRendimento(double taxa) {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Conta %s (%s) | Saldo: R$ %.2f",
                numero, getClass().getSimpleName().replace("Conta", ""), saldo);
    }
}