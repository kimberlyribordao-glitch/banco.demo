package com.banco.util;

public class CpfValidador {

    private CpfValidador() {}

    public static boolean valido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) return false;
        if (cpf.chars().distinct().count() == 1) return false;   // 11111111111, 00000000000...

        return calcularDigito(cpf, 9) == cpf.charAt(9) - '0'
                && calcularDigito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int calcularDigito(String cpf, int tamanho) {
        int soma = 0;
        int peso = tamanho + 1;
        for (int i = 0; i < tamanho; i++) {
            soma += (cpf.charAt(i) - '0') * peso--;
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}