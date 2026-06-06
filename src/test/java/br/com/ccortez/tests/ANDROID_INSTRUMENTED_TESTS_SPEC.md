# Especificação de testes instrumentados (espelho do `AndroidAppTests`)

Este documento resume o que os testes E2E com Appium já validam no app **Jetpack Compose** alvo (`Taxi`; ver `readme.md` do repositório). Use-o como insumo para implementar os mesmos cenários como **Android instrumented tests** (por exemplo Espresso + `compose.ui.test` ou UI Automator, conforme o padrão do projeto Android).

**Origem espelho:** pacote Java `AndroidAppTests` nesta mesma pasta.  
**Identificadores do pacote:** `applicationId`/recursos vistas nos testes: `br.com.ccortez.taxi` (suffixo `:id/` nas views com `resource-id`).

---

## Escopo atual dos testes E2E (`AndroidAppTests`)

| Prioridade/caso | Objetivo | Resultado esperado |
|-----------------|----------|---------------------|
| Título da tela | Garantir que a tela de solicitação de viagem carrega e mostra o título correto | O texto visível do título é **exatamente** `Travel Request` |
| Fluxo ponta a ponta | Preencher ID e endereços, enviar solicitação e navegar para a próxima tela | Após clicar em solicitar viagem, a tela seguinte mostra o título **exactamente** `Available Riders` |

### Dados fixos do fluxo E2E

Use os mesmos valores para manter comportamento determinístico e comparável com o smoke E2E:

| Campo (rótulo de acessibilidade / semântica na UI) | Valor digitado |
|----------------------------------------------------|----------------|
| Id do usuário | `12345` |
| Endereço de origem | `Av. Brasil, 2033 - Jardim America, São Paulo - SP, 01431-001` |
| Endereço de destino | `Av. Paulista, 1538 - Bela Vista, São Paulo - SP, 01310-200` |

### Textos esperados (assertivas literais)

- Título primeira tela: `Travel Request`
- Título segunda tela (após sucesso na navegação): `Available Riders`

---

## Mapeamento de localizadores (Appium → testes instrumentados)

No driver Espresso dentro do Appium, os elementos **Compose** costumam aparecer assim:

### Tela «Travel Request»

| Papel na spec | Como o E2E localiza (`TravelRequestPage`) | Sugestão no app Android instrumentado |
|---------------|--------------------------------------------|----------------------------------------|
| Título da tela | Tag de semântica / `tag name`: **`travelRequestTitle`** | Matcher em `SemanticsNode`/`testTag` equivalente (**`travelRequestTitle`**): preferir esperar até **visível** antes de ler texto (Compose pode estar na árvore antes de estar desenhado). |
| Campo usuário | Tag com nome **`Id do usuário`** | Mesmo contrato semântico; em Compose: campo associado ao rótulo / `contentDescription` / `testTag` — alinhar com o que o app expõe hoje ao Appium. |
| Campo origem | Tag **`Endereço de origem`** | Idem. |
| Campo destino | Tag **`Endereço de destino`** | Idem. |
| Botão solicitar | Tag **`requestTravelButton`** | Clicável após estar visível/habilitado. |

### Tela «Available Riders»

| Papel na spec | Como o E2E localiza (`RiderOptionsPage`) | Sugestão no app Android instrumentado |
|---------------|------------------------------------------|----------------------------------------|
| Título da lista | Tag **`availableRidersTitle`** | Esperar **visível** antes de ler texto (mesmo motivo do Compose). |
| Cartão de motorista (índice *n*) | `resource-id`: `…:id/riderOptionCard_<n>` | Usar `:id/` + mesmo sufixo se mantiver Views com id. |
| Nome, descrição, veículo, avaliação, preço (*n*) | `riderOptionName_<n>`, `riderOptionDescription_<n>`, `riderOptionVehicle_<n>`, `riderOptionRating_<n>`, `riderOptionPrice_<n>` | Opcional nos cenários espelho atuais; úteis para testes extras. |
| Confirmar corrida (*n*) | `confirmRideButton_<n>` | Opcional para fluxos além dos dois cenários cobertos pelo `AndroidAppTests`. |
| Estado carregamento / erro / lista vazia | `loadingIndicatorWithText`, `errorScreen`, `emptyListScreen` | Não asserted no `AndroidAppTests`; disponíveis para cenários futuros no módulo `androidTest`. |
| Diálogo de confirmação | `confirmDialogTitle`, `confirmDialogText`, `confirmDialogConfirmButton` | Idem — extras. |

Prefixo numérico de resource-id esperado pelo page object atual:  
`br.com.ccortez.taxi:id/<nome_do_recurso>`

---

## Comportamento de espera / flakiness (importante espelhar)

1. **Títulos**: Não usar apenas “presente na árvore”; aguardar **visibilidade** antes de ler texto (nos page objects Appium já se usa `visibilityOfElementLocated` para títulos exatamente por isso).

2. **Após clicar «solicitar viagem»**: O E2E **não** usa `sleep` fixo; a estabilidade vem da espera explícita no título **`availableRidersTitle`** (até ~10–15 s de timeout equivale bem a `waitUntil`/idle no Compose instrumentado).

3. **Screenshots**: O método `takeScreenshot` do `AndroidAppTests` é auxiliar ao E2E; em instrumentados o equivalente costuma ser `takeScreenshot`/Device Artifacts conforme Gradle — opcional por cenário.

---

## Checklist mínimo sugerido no módulo Android (`androidTest`)

- [ ] Classe(es) `@RunWith` JUnit ou robolectric-conform conforme projeto; Compose: `createAndroidComposeRule` ou regra combinada conforme já usado no app Taxi.

- [ ] Teste **1**: abertura/rota inicial → assert título **`Travel Request`**.

- [ ] Teste **2**: preencher os três campos com os dados da tabela acima → clicar botão **`requestTravelButton`** → assert título **`Available Riders`**.

Opcionalmente documentar nos comentários do PR que esta spec é derivada de `ANDROID_INSTRUMENTED_TESTS_SPEC.md` no repositório `native-appium-demo` para rastreabilidade entre camadas E2E e instrumentada.
