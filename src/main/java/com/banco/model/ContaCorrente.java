package com.banco.model;

import com.banco.exception.SaldoInsuficienteException;
import jakarta.persistence.Entity;

@Entity
public class ContaCorrente extends Conta {
    private double limite = 500;

    public ContaCorrente() {}

    public ContaCorrente(String numero, Cliente cliente) {
        super(numero, cliente);
    }

    @Override
    public void sacar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor inválido.");
        if (valor > getSaldo() + limite) throw new SaldoInsuficienteException("Saldo + limite insuficientes.");
        setSaldo(getSaldo() - valor);
    }
    public double getLimite() { return limite; }
}