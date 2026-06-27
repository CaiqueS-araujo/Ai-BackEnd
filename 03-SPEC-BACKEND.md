# 03 — SPEC BACKEND (Spring Boot)

> Implementa o [`01-CONTRATO.md`](./01-CONTRATO.md). Regra de ouro:
> **controllers são fronteira HTTP; services detêm o domínio.**

---

## 1. Stack

| Camada | Escolha | Por quê |
|---|---|---|
| Linguagem | Java 17 | LTS; records p/ DTOs. |
| Framework | Spring Boot 3.5.x | Web + injeção + validação. |
| Web | Spring Web (MVC) | Controllers REST. |
| Persistência | Spring Data JPA | Repositórios declarativos. |
| Banco (dev) | H2 (arquivo) | Relacional, zero setup. |
| Banco (prod-ready) | PostgreSQL | Troca via `application.yml`. |
| Validação | Bean Validation (`jakarta.validation`) | `@NotBlank`, etc. |
| Build | Maven | `mvn spring-boot:run`. |
| Testes | JUnit 5 + Spring Test + MockMvc | Slice de controller + unit de service. |

---

## 2. Arquitetura em camadas (isolamento rígido)

```
HTTP ──▶ Controller ──▶ Service ──▶ Repository ──▶ Banco
            │              │
        (fronteira)   (domínio puro:
         só traduz     sem HTTP, sem
         req/resp)     detalhe de DB)
```

- **Controller**: recebe DTO, chama service, devolve DTO + status. Sem `if` de regra.
- **Service**: orquestra entidades e regras; recebe/devolve **tipos de domínio ou DTOs**, nunca `HttpServletRequest`.
- **Repository**: interfaces Spring Data.
- **Mapper**: converte entidade ↔ DTO (mantém entidade fora da fronteira).
- **Exception handler**: `@RestControllerAdvice` central → envelope de erro do contrato.

---

## 3. Estrutura de pastas

```
src/main/java/com/serratec/chat/
├── ChatApplication.java
├── controller/
│   ├── ConversationController.java   # POST/GET /api/conversations
│   ├── MessageController.java        # GET/POST .../{id}/messages
│   ├── AttachmentController.java     # POST .../{id}/attachments
│   └── HealthController.java         # GET /api/health
├── service/
│   ├── ConversationService.java
│   ├── MessageService.java
│   ├── AttachmentService.java
│   ├── HealthService.java
│   └── chat/
│       ├── ChatResponseProvider.java      # interface (porta p/ IA futura)
│       └── StubChatResponseProvider.java  # impl determinística desta fase
├── repository/
│   ├── ConversationRepository.java
│   ├── MessageRepository.java
│   └── AttachmentRepository.java
├── domain/
│   ├── Conversation.java
│   ├── Message.java
│   ├── Attachment.java
│   └── MessageRole.java              # enum USER, ASSISTANT
├── dto/
│   ├── request/
│   │   ├── CreateConversationRequest.java
│   │   └── SendMessageRequest.java
│   └── response/
│       ├── ConversationResponse.java
│       ├── ConversationSummaryResponse.java
│       ├── MessageResponse.java
│       ├── SendMessageResponse.java
│       ├── AttachmentResponse.java
│       └── HealthResponse.java
├── mapper/
│   ├── ConversationMapper.java
│   ├── MessageMapper.java
│   └── AttachmentMapper.java
├── config/
│   ├── CorsConfig.java               # origem do front
│   └── WebConfig.java                # limites de multipart
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── UnsupportedFileTypeException.java

src/main/resources/
└── application.yml

src/test/java/com/serratec/chat/
├── controller/   # MockMvc slice tests
└── service/      # unit tests
```

---

## 4. Domínio (entidades JPA)

**`Conversation`**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `UUID` | PK; gerado pela aplicação. |
| `title` | `String` | default `"Nova conversa"`. |
| `createdAt` | `Instant` | `@CreationTimestamp`. |
| `updatedAt` | `Instant` | `@UpdateTimestamp`. |
| `messages` | `List<Message>` | `@OneToMany(mappedBy="conversation")`. |
| `attachments` | `List<Attachment>` | `@OneToMany(mappedBy="conversation")`. |

**`Message`**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `UUID` | PK. |
| `conversation` | `Conversation` | `@ManyToOne`, FK obrigatória. |
| `role` | `MessageRole` | `@Enumerated(STRING)`. |
| `content` | `String` | `@Column(columnDefinition="TEXT")`. |
| `createdAt` | `Instant` | `@CreationTimestamp`. |

**`Attachment`**

| Campo | Tipo | Notas |
|---|---|---|
| `id` | `UUID` | PK. |
| `conversation` | `Conversation` | `@ManyToOne`, FK obrigatória. |
| `filename` | `String` | nome original. |
| `contentType` | `String` | `text/plain` ou `application/pdf`. |
| `sizeBytes` | `long` | tamanho. |
| `storagePath` | `String` | caminho local; nullable (mínimo = só metadados). |
| `createdAt` | `Instant` | `@CreationTimestamp`. |

> O **ID da conversa é o ID de sessão** do contrato. Persistência relacional
> básica por sessão = histórico recuperável por `conversationId`.

---

## 5. Services (domínio)

| Service | Métodos | Regras |
|---|---|---|
| `ConversationService` | `create(title?)`, `listSummaries()`, `getOrThrow(id)` | Title default; `ResourceNotFoundException` se ausente. |
| `MessageService` | `history(conversationId)`, `send(conversationId, content)` | Valida conversa; persiste msg USER; obtém resposta via `ChatResponseProvider`; persiste msg ASSISTANT; toca `updatedAt`. Retorna ambas. |
| `AttachmentService` | `upload(conversationId, MultipartFile)` | Valida conversa, tipo, extensão, tamanho; grava metadados (e arquivo se storage habilitado). |
| `HealthService` | `check()` | Monta `HealthResponse`; checa conectividade do banco → `checks.database`. |

### 5.1 Seam de IA — `ChatResponseProvider`

```java
public interface ChatResponseProvider {
    String generateReply(String userContent, Conversation context);
}
```

`StubChatResponseProvider` (`@Service` ativo nesta fase) retorna algo
**determinístico**, ex.:
```
"Recebi sua mensagem: \"<conteúdo>\". [resposta de teste]"
```

> Trocar por um modelo real = adicionar outra implementação e injetá-la (ex.:
> `@Primary` ou `@ConditionalOnProperty`). **Controller e contrato não mudam.**

---

## 6. Controllers (fronteira HTTP)

| Controller | Endpoint(s) | Faz |
|---|---|---|
| `ConversationController` | `POST /api/conversations`, `GET /api/conversations` | Cria (201) / lista (200). |
| `MessageController` | `GET .../{id}/messages`, `POST .../{id}/messages` | Histórico (200) / envio (201). `@Valid` no `SendMessageRequest`. |
| `AttachmentController` | `POST .../{id}/attachments` (consumes `multipart/form-data`) | Recebe `@RequestParam("file") MultipartFile` → 201. |
| `HealthController` | `GET /api/health` | 200 (UP) / 503 (DOWN). |

Nenhum controller contém lógica de negócio — apenas delega ao service e mapeia o status.

---

## 7. Tratamento de erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) traduz exceções no envelope do contrato:

| Exceção | Status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `MethodArgumentNotValidException` / `ConstraintViolationException` | 400 |
| `UnsupportedFileTypeException` | 415 |
| `MaxUploadSizeExceededException` | 413 |
| `Exception` (fallback) | 500 |

---

## 8. Persistência & validação de arquivo

- `AttachmentService` valida **antes** de persistir: `contentType ∈ {text/plain, application/pdf}` **e** extensão `∈ {.txt, .pdf}` **e** `size ≤ app.storage.max-file-size`.
- Mínimo: registrar metadados. Recomendado: salvar bytes em `app.storage.dir` e gravar `storagePath`.
- O contrato exige **somente metadados** no retorno — bytes ficam fora do payload.

---

## 9. Configuração (`application.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/chatdb
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

app:
  cors:
    allowed-origins: http://localhost:5173
  storage:
    enabled: true
    dir: ./uploads
    max-file-size: 10485760   # 10 MB em bytes
```

`CorsConfig` lê `app.cors.allowed-origins`; `WebConfig`/multipart aplica os limites.

---

## 10. Critérios de pronto do back

- [ ] Nenhum controller contém regra de negócio (só delega + mapeia status).
- [ ] Nenhum service referencia tipos de HTTP (`HttpServletRequest`, `ResponseEntity` fora do controller).
- [ ] Os 6 endpoints respondem **exatamente** conforme o contrato (status + shape).
- [ ] Erros saem no envelope único (§7) com o status correto.
- [ ] Upload aceita só `.txt`/`.pdf` ≤ 10MB e registra metadados.
- [ ] Histórico persistido e recuperável por `conversationId`.
- [ ] `GET /api/health` estável (200 UP / 503 DOWN com check de banco).
- [ ] `ChatResponseProvider` isola a resposta de teste do resto.
- [ ] CORS habilitado para a origem do front.
- [ ] Testes: slice MockMvc por controller + unit por service.
