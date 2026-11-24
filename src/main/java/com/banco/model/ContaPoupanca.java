package com.banco.model;

import com.banco.exception.SaldoInsuficienteException;
import jakarta.persistence.Entity;

@Entity
public class ContaPoupanca extends Conta {

    public ContaPoupanca() {}

    public ContaPoupanca(String numero, Cliente cliente) {
        super(numero, cliente);
    }

    @Override
    public void sacar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor inválido.");
        if (valor > getSaldo()) throw new SaldoInsuficienteException("Saldo insuficiente.");
        setSaldo(getSaldo() - valor);
    }
    @Override
    public double calcularRendimento(double taxa) {
        return getSaldo() * taxa;
    }
}