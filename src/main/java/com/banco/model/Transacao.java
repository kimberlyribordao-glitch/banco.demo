package com.banco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Transacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    private double valor;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    public Transacao() {
    }

    // Usado ao criar uma nova transação
    public Transacao(TipoTransacao tipo, double valor, Conta conta) {
        this.tipo = tipo;
        this.valor = valor;
        this.conta = conta;
        this.dataHora = LocalDateTime.now();
    }

    // Usado pelo JDBC ao montar o extrato
    public Transacao(Long id, TipoTransacao tipo, double valor, LocalDateTime dataHora) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Conta getConta() {
        return conta;
    }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("%s | %-22s | R$ %.2f", dataHora.format(f), tipo, valor);
    }
}