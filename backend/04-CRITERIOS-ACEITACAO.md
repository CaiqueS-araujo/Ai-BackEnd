# 04 — CRITÉRIOS DE ACEITAÇÃO & DEFINITION OF DONE

> Mapeia os **Requisitos Mínimos** do enunciado para critérios verificáveis.
> Formato Given/When/Then. Use como checklist de validação pós-geração no opencode.

---

## 1. Requisitos mínimos → critérios

### 1.1 Mensageria Base (Chat)

> *Fluxo HTTP síncrono; cliente envia mensagens e renderiza respostas textuais
> via Spring Boot (respostas controladas de teste).*

- **CA-1.1** — **Given** uma conversa existente, **When** o usuário envia um texto não-vazio, **Then** o back persiste a msg `USER`, gera a resposta de teste, persiste a msg `ASSISTANT` e retorna **201** com as duas mensagens.
- **CA-1.2** — **Given** a resposta da API, **When** ela chega ao front, **Then** as duas bolhas são renderizadas (`USER` à direita, `ASSISTANT` à esquerda).
- **CA-1.3** — **Given** `content` vazio, **When** enviado, **Then** o back retorna **400** no envelope de erro e o front bloqueia o envio.
- **CA-1.4** — A resposta de teste vem do `ChatResponseProvider`; substituí-lo por IA **não** altera contrato nem controller.

### 1.2 Recepção de Documentos

> *Drag-and-drop + barra de progresso; endpoint multipart aceitando `.txt`/`.pdf`;
> registra metadados.*

- **CA-2.1** — **Given** a área de upload, **When** o usuário arrasta **ou** clica e escolhe um arquivo, **Then** ambos os caminhos funcionam.
- **CA-2.2** — **Given** um `.txt` ou `.pdf` ≤ 10MB, **When** enviado, **Then** a barra de progresso evolui 0→100% e o back retorna **201** com os metadados (`filename`, `contentType`, `sizeBytes`).
- **CA-2.3** — **Given** um arquivo de tipo/extensão não suportado, **When** selecionado, **Then** o front rejeita **antes** de enviar e o back, se acionado, retorna **415**.
- **CA-2.4** — **Given** arquivo acima de 10MB, **Then** retorno **413** (ou bloqueio client-side).

### 1.3 Persistência e Histórico

> *Ciclo de vida das conversas por ID de sessão; histórico completo recuperável.*

- **CA-3.1** — **Given** mensagens trocadas numa sessão, **When** `GET /api/conversations/{id}/messages`, **Then** retorna o histórico completo em ordem cronológica.
- **CA-3.2** — **Given** o app reaberto, **When** a conversa é selecionada, **Then** o histórico persistido reaparece (sobrevive a restart do servidor — H2 em arquivo).
- **CA-3.3** — **Given** várias conversas, **When** `GET /api/conversations`, **Then** lista por `updatedAt` desc com `messageCount` e preview.

### 1.4 Monitoramento Operacional

> *`GET /api/health` obrigatório como base de monitoramento.*

- **CA-4.1** — **Given** o serviço no ar, **When** `GET /api/health`, **Then** **200** com `status: "UP"` e `checks.database`.
- **CA-4.2** — **Given** o banco indisponível, **Then** **503** com `status: "DOWN"`.
- **CA-4.3** — O front exibe o estado via `HealthBadge` (polling).

---

## 2. Critérios de arquitetura & qualidade

| ID | Critério |
|---|---|
| **AQ-1** | Backend: controllers sem regra de negócio; services sem tipos de HTTP. |
| **AQ-2** | Frontend: componentes sem `fetch`/regra; toda lógica em hooks. |
| **AQ-3** | Contrato é fonte única: shapes e status batem com `01-CONTRATO.md`. |
| **AQ-4** | Erros sempre no envelope único. |
| **AQ-5** | CORS configurado para a origem do front. |
| **AQ-6** | Cada repo tem `README.md` e `AGENTS.md` próprios. |
| **AQ-7** | A11y: `aria-live` no log de mensagens + navegação por teclado no chat/upload. |

---

## 3. Plano de testes mínimo

**Frontend (Vitest + RTL)**

| Alvo | Tipo | Verifica |
|---|---|---|
| `useChat` | unit (`renderHook`, API mockada) | envio otimista, append da resposta, erro/retry. |
| `useFileUpload` | unit | validação client-side + transições de `status`. |
| `useHealthCheck` | unit | polling e mapeamento `UP/DOWN`. |
| `MessageComposer` | render | Enter envia, Shift+Enter quebra, `disabled`. |
| `FileDropzone` | render | rejeita tipo inválido; aciona por teclado. |

**Backend (JUnit + MockMvc)**

| Alvo | Tipo | Verifica |
|---|---|---|
| `MessageController` | slice | 201 com 2 mensagens; 400 em vazio; 404 em conversa inexistente. |
| `AttachmentController` | slice | 201 metadados; 415 tipo inválido; 413 grande demais. |
| `HealthController` | slice | 200 UP / 503 DOWN. |
| `MessageService` | unit | persiste par USER/ASSISTANT; usa `ChatResponseProvider`. |
| `AttachmentService` | unit | regras de tipo/extensão/tamanho. |

---

## 4. Definition of Done (entrega da etapa)

- [ ] Todos os **CA-x** e **AQ-x** atendidos.
- [ ] Dois repositórios GitHub separados (front e back), cada um com `README.md` + `AGENTS.md`.
- [ ] `01-CONTRATO.md` validado e refletido nos dois lados.
- [ ] Suíte de testes mínima passando.
- [ ] App sobe localmente seguindo só o `README.md` de cada repo.
- [ ] `GET /api/health` respondendo e visível no front.
