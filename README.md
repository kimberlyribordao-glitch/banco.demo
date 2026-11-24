# Banco Java

Aplicação back-end de um banco, em Java puro (sem Spring), com menu interativo no console.

## Integrantes
- Kimberly Da Silva Ribordao
- [NOME DA DUPLA]

## Tema e problema
Uma agência pequena precisa controlar clientes, contas e movimentações financeiras. Feito em papel
ou planilha, isso gera erros de saldo, cadastros duplicados e falta de histórico. O sistema resolve
isso com cadastro de clientes, abertura de contas (corrente e poupança), depósitos, saques,
transferências, extratos e rendimento da poupança. Clientes excluídos ficam guardados por 30 dias
(exclusão lógica com período de retenção) antes da remoção definitiva.

## Tecnologias
Java 21, Maven, H2 (banco em arquivo), JPA/Hibernate e JDBC.

## Estrutura
```
com.banco
├── model        Cliente, Conta (abstrata), ContaCorrente, ContaPoupanca, Transacao, TipoTransacao
├── repository   Repositorio<T> (interface), ClienteRepository, ContaRepository (JPA), TransacaoRepository (JDBC)
├── service      ClienteService, ContaService (regras de negócio)
├── config       JPAUtil, ConexaoJDBC
├── exception    SaldoInsuficienteException
├── util         CpfValidador
└── Main         menu do console
```

## Requisitos atendidos
- **Entidades relacionadas:** Cliente 1—N Conta 1—N Transacao
- **CRUD:** Cliente (cadastrar, listar, atualizar, excluir)
- **Encapsulamento:** atributos privados com métodos de acesso
- **Herança e polimorfismo:** Conta → ContaCorrente / ContaPoupanca (`sacar()` e `calcularRendimento()`)
- **Abstração:** classe abstrata `Conta` e interface `Repositorio<T>`
- **JPA e JDBC:** JPA para clientes e contas, JDBC para transações e extratos
- **Camadas:** model, repository, service e Main

## Regras de negócio
- Nome só com letras; CPF com validação dos dígitos e sem duplicidade
- Conta corrente com limite de R$ 500; poupança sem saldo negativo
- Transferência entre contas com os dois saldos gravados na mesma transação
- Extrato completo e por período
- Rendimento mensal de 0,5% na poupança

## Como executar
1. Abrir o projeto no IntelliJ (JDK 21) e carregar o Maven
2. Executar a classe `com.banco.Main`
3. O banco é criado automaticamente na pasta `data/`
