package com.banco.service;

import com.banco.model.Cliente;
import com.banco.repository.ClienteRepository;
import com.banco.util.CpfValidador;

import java.util.List;

public class ClienteService {
    private static final int DIAS_RETENCAO = 30;

    private final ClienteRepository repository = new ClienteRepository();

    public void cadastrar(String nome, String cpf) {
        nome = validarNome(nome);

        // aceita CPF com pontos e traço (123.456.789-09) e guarda só os números
        if (cpf != null) cpf = cpf.replaceAll("[.\\-\\s]", "");
        if (cpf == null || !cpf.matches("\\d{11}"))
            throw new IllegalArgumentException("CPF deve ter 11 números.");
        if (!CpfValidador.valido(cpf))
            throw new IllegalArgumentException("CPF inválido.");

        Cliente existente = repository.buscarPorCpf(cpf);
        if (existente != null) {
            if (existente.isAtivo())
                throw new IllegalArgumentException("Já existe um cliente cadastrado com esse CPF.");
            throw new IllegalArgumentException(
                    "Esse CPF pertence a um cliente excluído, ainda em período de retenção.");
        }

        repository.salvar(new Cliente(nome, cpf));
    }

    public List<Cliente> listar() {
        return repository.listarTodos();
    }

    public List<Cliente> listarExcluidos() {
        return repository.listarExcluidos();
    }

    public Cliente buscar(Long id) {
        Cliente c = repository.buscarPorId(id);
        if (c == null || !c.isAtivo())
            throw new IllegalArgumentException("Cliente não encontrado.");
        return c;
    }

    public void atualizar(Long id, String novoNome) {
        novoNome = validarNome(novoNome);
        Cliente c = buscar(id);
        c.setNome(novoNome);
        repository.atualizar(c);
    }

    public void excluir(Long id) {
        buscar(id);
        repository.excluir(id);
    }

    public int limparExpirados() {
        return repository.purgarExpirados(DIAS_RETENCAO);
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        nome = nome.trim().replaceAll("\\s+", " ");
        if (!nome.matches("[\\p{L} ]+"))
            throw new IllegalArgumentException("Nome deve conter apenas letras (sem números ou símbolos).");
        if (nome.length() < 2)
            throw new IllegalArgumentException("Nome muito curto.");
        return nome;
    }
}