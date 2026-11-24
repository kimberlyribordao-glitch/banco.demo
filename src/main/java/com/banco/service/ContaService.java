package com.banco.service;

import com.banco.model.*;
import com.banco.repository.ClienteRepository;
import com.banco.repository.ContaRepository;
import com.banco.repository.TransacaoRepository;

import java.time.LocalDate;
import java.util.List;

public class ContaService {
    private static final double TAXA_RENDIMENTO = 0.005; // 0,5% ao mês

    private final ContaRepository contaRepository = new ContaRepository();
    private final ClienteRepository clienteRepository = new ClienteRepository();
    private final TransacaoRepository transacaoRepository = new TransacaoRepository();

    public Conta abrirConta(Long clienteId, String numero, boolean poupanca) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId);
        if (cliente == null || !cliente.isAtivo())
            throw new IllegalArgumentException("Cliente não encontrado.");
        if (numero == null || numero.isBlank())
            throw new IllegalArgumentException("Número da conta inválido.");
        if (contaRepository.buscarPorNumero(numero) != null)
            throw new IllegalArgumentException("Já existe conta com esse número.");

        Conta conta = poupanca ? new ContaPoupanca(numero, cliente) : new ContaCorrente(numero, cliente);
        contaRepository.salvar(conta);
        return conta;
    }

    public List<Conta> listarContas(Long clienteId) {
        return contaRepository.listarPorCliente(clienteId);
    }

    public Conta buscarPorNumero(String numero) {
        Conta conta = contaRepository.buscarPorNumero(numero);
        if (conta == null) throw new IllegalArgumentException("Conta não encontrada.");
        return conta;
    }

    public void depositar(String numero, double valor) {
        Conta conta = buscarPorNumero(numero);
        conta.depositar(valor);
        contaRepository.atualizar(conta);
        transacaoRepository.registrar(new Transacao(TipoTransacao.DEPOSITO, valor, conta));
    }

    public void sacar(String numero, double valor) {
        Conta conta = buscarPorNumero(numero);
        conta.sacar(valor);   // polimorfismo: cada tipo tem sua regra
        contaRepository.atualizar(conta);
        transacaoRepository.registrar(new Transacao(TipoTransacao.SAQUE, valor, conta));
    }

    public void transferir(String numeroOrigem, String numeroDestino, double valor) {
        if (numeroOrigem.equals(numeroDestino))
            throw new IllegalArgumentException("Origem e destino devem ser contas diferentes.");

        Conta origem = buscarPorNumero(numeroOrigem);
        Conta destino = buscarPorNumero(numeroDestino);

        origem.sacar(valor);        // se falhar (saldo insuficiente), nada foi salvo
        destino.depositar(valor);

        // os dois saldos são gravados na mesma transação JPA
        contaRepository.atualizarVarias(List.of(origem, destino));

        transacaoRepository.registrar(new Transacao(TipoTransacao.TRANSFERENCIA_ENVIADA, valor, origem));
        transacaoRepository.registrar(new Transacao(TipoTransacao.TRANSFERENCIA_RECEBIDA, valor, destino));
    }

    public List<Transacao> extrato(String numero) {
        Conta conta = buscarPorNumero(numero);
        return transacaoRepository.listarPorConta(conta.getId());
    }

    public List<Transacao> extratoPorPeriodo(String numero, LocalDate inicio, LocalDate fim) {
        if (fim.isBefore(inicio))
            throw new IllegalArgumentException("A data final não pode ser anterior à inicial.");
        Conta conta = buscarPorNumero(numero);
        return transacaoRepository.listarPorPeriodo(
                conta.getId(), inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
    }

    // Percorre TODAS as contas; só a poupança devolve rendimento > 0 (polimorfismo)
    public int aplicarRendimento() {
        int contas = 0;
        for (Conta conta : contaRepository.listarTodas()) {
            double rendimento = Math.round(conta.calcularRendimento(TAXA_RENDIMENTO) * 100) / 100.0;
            if (rendimento > 0) {
                conta.depositar(rendimento);
                contaRepository.atualizar(conta);
                transacaoRepository.registrar(new Transacao(TipoTransacao.RENDIMENTO, rendimento, conta));
                contas++;
            }
        }
        return contas;
    }
}
