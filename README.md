# Sistema Financeiro

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-blue?logo=apachemaven)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)

Um sistema financeiro pessoal feito em Java puro (sem Spring, sem banco), focado em modelar de verdade as regras chatas de um app de finanças: parcelamento, fatura de cartão de crédito, orçamento, metas, contas a pagar, transação recorrente etc.

A ideia não é ter uma API bonita ou uma tela pronta é entender e implementar direito as regras de negócio que todo app financeiro precisa resolver e que geralmente aparecem só quando o sistema já está em produção e alguém encontra o bug.

## Por que esse projeto existe

Comecei mexendo em domínio puro em Java (sem framework no meio) pra treinar modelagem: agregados, invariantes, validação na criação do objeto em vez de espalhada em serviços, etc. Fui adicionando regra por regra conforme fazia sentido de um sistema financeiro real tipo aquelas que você só descobre que precisa quando alguém reclama "por que minha parcela de março já apareceu na fatura de janeiro?".

Não tem banco de dados, não tem API REST, não tem tela. É tudo em memória, de propósito, pra manter o foco 100% no domínio antes de pensar em infraestrutura.

## Regras de negócio implementadas

- **Contas** — saldo, cheque especial, ativar/inativar, transferência entre contas. Conta com transação vinculada não pode ser excluída, só inativada.
- **Categorias** — receita e despesa, com subcategorias. Categoria de despesa não entra em transação de receita (e vice-versa), e subcategoria só pode ter o mesmo tipo da categoria pai.
- **Transações** — status `SCHEDULED`, `PENDING`, `COMPLETED`, `CANCELLED`, `REVERSED`. Transação efetivada não pode ter data futura; se a data é futura, só pode estar pendente ou agendada.
- **Compra parcelada** — gera todas as parcelas de uma vez no cadastro, mas cada parcela só entra no saldo/fatura na sua própria data de vencimento, não todas juntas.
- **Cartão de crédito e fatura** — ao fechar a fatura, qualquer compra feita depois (mesmo que a fatura ainda não tenha sido fechada "no relógio") é direcionada automaticamente pra fatura seguinte.
- **Orçamento por categoria** — estourar o orçamento nunca bloqueia o lançamento da despesa, só acende o alerta (`isOverBudget` / valor excedente).
- **Metas financeiras** — o valor atual nunca ultrapassa o valor alvo, mesmo contribuindo mais do que falta.
- **Contas a pagar/receber** — viram "atrasada" sozinhas quando a data passa do vencimento sem pagamento, sem precisar de ninguém marcar manualmente.
- **Transações recorrentes** — nunca geram duas ocorrências pra mesma competência (mês), mesmo que o processo de geração rode mais de uma vez em cima da mesma data.
- **Transferência entre contas** — neutra nos relatórios: não conta como receita nem como despesa por categoria, nem no fluxo de caixa consolidado.
- **Dinheiro em `BigDecimal`** — sem `double`/`float` em nenhum lugar. Divisão de parcelas sempre fecha exatamente o valor total (o resto de centavos vai pra última parcela).

## Como rodar

Precisa de Java 21 e Maven.

```bash
mvn compile
mvn exec:java -Dexec.mainClass="io.github.unerdarks.Main"
```

A `Main` roda um cenário de ponta a ponta passando por cada uma das regras acima e imprime o resultado no console — é a forma mais rápida de ver o sistema funcionando sem precisar escrever teste ainda.

## Estrutura do projeto

```
io.github.unerdarks
├── Main.java
├── enums/          # AccountType, TransactionType, TransactionStatus, BillStatus...
├── models/         # Account, Transaction, Category, CreditCard, Invoice, Budget, FinancialGoal, Bill, RecurringTransaction, InstallmentPurchase
├── repositories/   # repositórios em memória (List), um por agregado
├── services/       # orquestram os models e os repositórios
└── utils/          # CrudService (contrato genérico de CRUD)
```

Os models concentram a regra de negócio de verdade (validação e invariantes acontecem dentro deles, no `create`/nos métodos de mutação). Os services só orquestram: buscam no repositório, chamam o model, salvam de volta. Nada de regra de negócio escondida em service.

## Autor

Feito por [UnerDarks](https://github.com/UnerDarks) estudando Java e modelagem de domínio.
