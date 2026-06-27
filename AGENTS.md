# AGENTS.md — Repositório Backend

> Documenta o **escopo de atuação do agente** (opencode) e os **prompts-base
> CRISP** usados para gerar o código estrutural a partir das specs. Exigido pelo
> padrão de entrega sempre que ferramentas de IA gerarem artefatos de código.

---

## 1. Contexto do repositório

API REST em Spring Boot 3.5 (Java 17) que implementa `01-CONTRATO.md`.
Arquitetura em camadas com isolamento rígido: **controllers são fronteira HTTP;
services detêm o domínio.**

Specs de referência (contexto-base do agente):
- `01-CONTRATO.md` — contrato (fonte da verdade)
- `03-SPEC-BACKEND.md` — arquitetura do back
- `04-CRITERIOS-ACEITACAO.md` — critérios de validação

---

## 2. Escopo do agente

**Pode:** criar a estrutura de `03-SPEC-BACKEND.md §3`; gerar entidades,
repositórios, services, controllers, DTOs (records), mappers, configs (CORS,
multipart), exception handler e testes.

**Não pode (guardrails):**
- Colocar regra de negócio em controllers.
- Referenciar tipos de HTTP dentro de services.
- Inventar campos/endpoints fora de `01-CONTRATO.md`.
- Expor a entidade na fronteira (sempre DTO via mapper).
- Acoplar a resposta de teste fora do `ChatResponseProvider`.

---

## 3. Padrão de prompt — CRISP

Todo prompt segue **C**ontexto · **R**ole (papel) · **I**nstruções ·
**S**pecifics (especificidade) · **P**arâmetros. *(Ajuste os rótulos se a sua
turma definir o CRISP de forma diferente — a estrutura permanece.)*

---

## 4. Prompts-base (executar nesta ordem)

### Prompt 1 — Projeto + domínio + persistência
```
[Contexto] Repositório back de um sistema de chat. Specs: 01-CONTRATO.md e
03-SPEC-BACKEND.md são a verdade.
[Role] Aja como dev back-end sênior especialista em Spring Boot + JPA.
[Instruções] Crie o projeto Maven (Spring Web, Spring Data JPA, Validation, H2) e
implemente as entidades Conversation, Message, Attachment, o enum MessageRole e os
repositórios da §3/§4.
[Specifics] IDs UUID gerados na aplicação; timestamps com @CreationTimestamp/
@UpdateTimestamp; relações @OneToMany/@ManyToOne conforme §4. application.yml igual
ao §9 (H2 em arquivo, multipart 10MB, app.cors, app.storage).
[Parâmetros] Java 17; open-in-view=false; sem lógica fora das camadas previstas.
```

### Prompt 2 — Services + seam de IA
```
[Contexto] Domínio e repositórios prontos. Spec: 03-SPEC-BACKEND.md §5.
[Role] Dev back-end sênior com foco em isolamento de domínio.
[Instruções] Implemente ConversationService, MessageService, AttachmentService,
HealthService, a interface ChatResponseProvider e StubChatResponseProvider.
[Specifics] Services agnósticos a HTTP. MessageService persiste par USER/ASSISTANT
e obtém a resposta via ChatResponseProvider (stub determinístico). AttachmentService
valida tipo/extensão/tamanho antes de persistir. HealthService checa o banco.
[Parâmetros] Nenhum tipo de HTTP nos services; exceções de domínio
(ResourceNotFoundException, UnsupportedFileTypeException).
```

### Prompt 3 — Controllers + DTOs + mappers + erros + CORS
```
[Contexto] Services prontos. Specs: 01-CONTRATO.md (todos os endpoints) e
03-SPEC-BACKEND.md §6, §7.
[Role] Dev back-end sênior especialista em REST com Spring MVC.
[Instruções] Gere DTOs (records request/response), mappers, os 4 controllers, o
GlobalExceptionHandler e CorsConfig/WebConfig.
[Specifics] Controllers só delegam ao service e mapeiam status (201/200/404/400/
413/415). Erros saem no envelope único. AttachmentController consome
multipart/form-data com @RequestParam("file"). CORS lê app.cors.allowed-origins.
[Parâmetros] Zero regra em controller; entidade nunca cruza a fronteira (só DTO).
```

### Prompt 4 — Testes
```
[Contexto] API completa. Spec: 04-CRITERIOS-ACEITACAO.md §3 (bloco Backend).
[Role] Dev sênior em testes com JUnit 5 + Spring Test + MockMvc.
[Instruções] Gere os testes da tabela: slice MockMvc por controller + unit por
service.
[Specifics] MessageController: 201 com 2 mensagens, 400 vazio, 404 inexistente.
AttachmentController: 201 metadados, 415 tipo inválido, 413 grande. HealthController:
200 UP / 503 DOWN. Services com colaboradores mockados.
[Parâmetros] Sem dependência de rede externa; banco de teste isolado.
```

---

## 5. Validação pós-geração

Rodar o checklist de `03-SPEC-BACKEND.md §10` e os critérios `AQ-1`, `AQ-3`,
`AQ-4`, `AQ-5` de `04-CRITERIOS-ACEITACAO.md`. Conferir cada endpoint contra
`01-CONTRATO.md` (status + shape) antes de considerar pronto.
