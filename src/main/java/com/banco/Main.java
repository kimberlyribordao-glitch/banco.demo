package com.banco;

import com.banco.config.JPAUtil;
import com.banco.model.Cliente;
import com.banco.model.Conta;
import com.banco.model.Transacao;
import com.banco.service.ClienteService;
import com.banco.service.ContaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    // Reduz o ruído dos logs do Hibernate (mantém avisos e erros). A referência evita que o Java descarte a configuração.
    private static final Logger LOG_HIBERNATE = Logger.getLogger("org.hibernate");
    static {
        LOG_HIBERNATE.setLevel(Level.WARNING);
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static final ClienteService clienteService = new ClienteService();
    private static final ContaService contaService = new ContaService();
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {
        System.out.println("Iniciando o sistema...");
        int removidos = clienteService.limparExpirados();
        if (removidos > 0) {
            System.out.println(removidos + " cliente(s) com retenção vencida foram removidos.");
        }

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("\n>> Escolha uma opção: ");
            try {
                executar(opcao);
            } catch (Exception e) {
                System.out.println("\n❌ ERRO: " + e.getMessage());
            }
        } while (opcao != 0);

        JPAUtil.fechar();
        System.out.println("Sistema encerrado.");
    }

    // ---------- menu ----------

    private static void exibirMenu() {
        System.out.println();
        System.out.println("╔" + "═".repeat(42) + "╗");
        linha("               BANCO JAVA");
        divisor();
        linha("  CLIENTES");
        linha("    1  - Cadastrar cliente");
        linha("    2  - Cadastrar vários clientes");
        linha("    3  - Listar clientes");
        linha("    4  - Atualizar cliente");
        linha("    5  - Excluir cliente");
        divisor();
        linha("  CONTAS");
        linha("    6  - Abrir conta");
        linha("    7  - Listar contas de um cliente");
        divisor();
        linha("  MOVIMENTAÇÕES");
        linha("    8  - Depositar");
        linha("    9  - Sacar");
        linha("    10 - Transferir entre contas");
        linha("    11 - Extrato completo");
        linha("    12 - Extrato por período");
        divisor();
        linha("  ADMINISTRAÇÃO");
        linha("    13 - Aplicar rendimento da poupança");
        linha("    14 - Clientes excluídos (retenção)");
        linha("    15 - Apagar expirados definitivamente");
        divisor();
        linha("    0  - Sair");
        System.out.println("╚" + "═".repeat(42) + "╝");
    }

    private static void linha(String texto) {
        System.out.println("║" + String.format("%-42s", texto) + "║");
    }

    private static void divisor() {
        System.out.println("╠" + "═".repeat(42) + "╣");
    }

    private static void executar(int opcao) {
        switch (opcao) {
            case 1 -> cadastrarCliente();
            case 2 -> cadastrarVariosClientes();
            case 3 -> listarClientes();
            case 4 -> atualizarCliente();
            case 5 -> excluirCliente();
            case 6 -> abrirConta();
            case 7 -> listarContas();
            case 8 -> depositar();
            case 9 -> sacar();
            case 10 -> transferir();
            case 11 -> extrato();
            case 12 -> extratoPorPeriodo();
            case 13 -> aplicarRendimento();
            case 14 -> listarExcluidos();
            case 15 -> limparExpirados();
            case 0 -> { }
            default -> System.out.println("Opção inválida.");
        }
    }

    // ---------- clientes ----------

    private static void cadastrarCliente() {
        String nome = lerTexto("Nome: ");
        String cpf = lerTexto("CPF (11 números): ");
        clienteService.cadastrar(nome, cpf);
        System.out.println("✅ Cliente cadastrado com sucesso!");
    }

    private static void cadastrarVariosClientes() {
        System.out.println("\n--- Cadastro em lote ---");
        int cadastrados = 0;
        int falhas = 0;

        while (true) {
            String nome = lerTexto("Nome (0 para sair): ");
            if (nome.equals("0")) break;
            String cpf = lerTexto("CPF (11 números): ");

            try {
                clienteService.cadastrar(nome, cpf);
                cadastrados++;
                System.out.println("✅ Cadastrado!\n");
            } catch (Exception e) {
                falhas++;
                System.out.println("❌ ERRO: " + e.getMessage() + " (tente de novo)\n");
            }
        }
        System.out.println("\nResumo: " + cadastrados + " cadastrado(s), " + falhas + " erro(s).");
    }

    private static void listarClientes() {
        List<Cliente> lista = clienteService.listar();
        if (lista.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        lista.forEach(System.out::println);
    }

    private static void atualizarCliente() {
        Long id = lerLong("ID do cliente: ");
        String novoNome = lerTexto("Novo nome: ");
        clienteService.atualizar(id, novoNome);
        System.out.println("✅ Cliente atualizado!");
    }

    private static void excluirCliente() {
        Long id = lerLong("ID do cliente: ");
        clienteService.excluir(id);
        System.out.println("✅ Cliente excluído! Os dados ficam guardados durante o período de retenção.");
    }

    private static void listarExcluidos() {
        List<Cliente> lista = clienteService.listarExcluidos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum cliente em período de retenção.");
            return;
        }
        lista.forEach(c -> System.out.println(c + " | excluído em " + c.getDataExclusao()));
    }

    private static void limparExpirados() {
        int removidos = clienteService.limparExpirados();
        System.out.println(removidos + " cliente(s) removido(s) definitivamente.");
    }

    // ---------- contas ----------

    private static void abrirConta() {
        Long clienteId = lerLong("ID do cliente: ");
        String numero = lerTexto("Número da conta: ");
        int tipo = lerInteiro("Tipo (1 - Corrente, 2 - Poupança): ");
        Conta conta = contaService.abrirConta(clienteId, numero, tipo == 2);
        System.out.println("✅ Conta aberta: " + conta);
    }

    private static void listarContas() {
        Long clienteId = lerLong("ID do cliente: ");
        List<Conta> contas = contaService.listarContas(clienteId);
        if (contas.isEmpty()) {
            System.out.println("Esse cliente não tem contas.");
            return;
        }
        contas.forEach(System.out::println);
    }

    // ---------- movimentações ----------

    private static void depositar() {
        String numero = lerTexto("Número da conta: ");
        double valor = lerDouble("Valor do depósito: ");
        contaService.depositar(numero, valor);
        System.out.println("✅ Depósito realizado!");
    }

    private static void sacar() {
        String numero = lerTexto("Número da conta: ");
        double valor = lerDouble("Valor do saque: ");
        contaService.sacar(numero, valor);
        System.out.println("✅ Saque realizado!");
    }

    private static void transferir() {
        String origem = lerTexto("Conta de origem: ");
        String destino = lerTexto("Conta de destino: ");
        double valor = lerDouble("Valor da transferência: ");
        contaService.transferir(origem, destino, valor);
        System.out.println("✅ Transferência realizada!");
    }

    private static void extrato() {
        String numero = lerTexto("Número da conta: ");
        List<Transacao> transacoes = contaService.extrato(numero);
        imprimirExtrato(numero, transacoes);
    }

    private static void extratoPorPeriodo() {
        String numero = lerTexto("Número da conta: ");
        LocalDate inicio = lerData("Data inicial (dd/MM/aaaa): ");
        LocalDate fim = lerData("Data final (dd/MM/aaaa): ");
        List<Transacao> transacoes = contaService.extratoPorPeriodo(numero, inicio, fim);
        imprimirExtrato(numero, transacoes);
    }

    private static void imprimirExtrato(String numero, List<Transacao> transacoes) {
        System.out.println("\n=== EXTRATO ===");
        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma movimentação.");
        } else {
            transacoes.forEach(System.out::println);
        }
        System.out.printf("Saldo atual: R$ %.2f%n", contaService.buscarPorNumero(numero).getSaldo());
    }

    // ---------- administração ----------

    private static void aplicarRendimento() {
        int contas = contaService.aplicarRendimento();
        System.out.println("✅ Rendimento aplicado em " + contas + " conta(s) poupança.");
    }

    // ---------- leitura segura do teclado ----------

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {
        try {
            return Integer.parseInt(lerTexto(mensagem));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Long lerLong(String mensagem) {
        try {
            return Long.parseLong(lerTexto(mensagem));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Digite um número válido.");
        }
    }

    private static double lerDouble(String mensagem) {
        try {
            return Double.parseDouble(lerTexto(mensagem).replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Digite um valor válido.");
        }
    }

    private static LocalDate lerData(String mensagem) {
        try {
            return LocalDate.parse(lerTexto(mensagem), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato dd/MM/aaaa.");
        }
    }
}
