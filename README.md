# Chat — Backend

API REST em Spring Boot que orquestra o domínio de um sistema de chat: fluxo de
conversas, recepção de anexos (`.txt` / `.pdf`) e monitoramento de saúde via
`GET /api/health`. As respostas do chat, nesta etapa, são **controladas/de teste**
atrás de uma interface preparada para ser trocada por um modelo de IA — sem alterar
contrato nem controllers.

> **Regra de ouro da arquitetura:** controllers são apenas fronteira HTTP;
> services detêm o domínio. Nenhuma regra de negócio nos controllers, e as
> entidades nunca cruzam a fronteira (sempre DTO via mapper).

---

## 🧱 Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5.x |
| Web | Spring Web (MVC) |
| Persistência | Spring Data JPA + Hibernate |
| Banco (dev) | H2 (arquivo) |
| Banco (prod-ready) | PostgreSQL |
| Validação | Bean Validation (`jakarta.validation`) |
| Build | Maven |
| Testes | JUnit 5 + Spring Test + MockMvc |

---

## 🏛️ Arquitetura

Fluxo de uma requisição, com isolamento rígido entre as camadas:

```
HTTP ──▶ Controller ──▶ Service ──▶ Repository ──▶ Banco (H2)
            │              │
        (fronteira)   (domínio puro:
         só traduz     sem HTTP, sem
         req/resp)     detalhe de DB)
```

- **Controller** — recebe DTO, delega ao service, devolve DTO + status HTTP. Zero regra.
- **Service** — orquestra entidades e regras de negócio; agnóstico a HTTP e a detalhes de banco.
- **Repository** — interfaces Spring Data JPA.
- **Mapper** — converte entidade ↔ DTO, mantendo a entidade fora da fronteira.
- **GlobalExceptionHandler** — traduz exceções no envelope de erro único.

---

## 📁 Estrutura

```
src/main/java/com/serratec/chat/
├── ChatApplication.java
├── controller/        # fronteira HTTP (sem regra de negócio)
│   ├── ConversationController.java
│   ├── MessageController.java
│   ├── AttachmentController.java
│   └── HealthController.java
├── service/           # domínio (agnóstico a HTTP)
│   ├── ConversationService.java
│   ├── MessageService.java
│   ├── AttachmentService.java
│   ├── HealthService.java
│   └── chat/
│       ├── ChatResponseProvider.java       # interface (porta para IA futura)
│       └── StubChatResponseProvider.java    # resposta de teste desta fase
├── repository/        # Spring Data JPA
│   ├── ConversationRepository.java
│   ├── MessageRepository.java
│   └── AttachmentRepository.java
├── domain/            # entidades + enum
│   ├── Conversation.java
│   ├── Message.java
│   ├── Attachment.java
│   └── MessageRole.java          # USER | ASSISTANT
├── dto/
│   ├── request/       # CreateConversationRequest, SendMessageRequest
│   └── response/      # ConversationResponse, MessageResponse, SendMessageResponse, ...
├── mapper/            # entidade ↔ DTO
├── config/            # CorsConfig, WebConfig (multipart)
└── exception/         # GlobalExceptionHandler + exceções de domínio

src/main/resources/
└── application.yml
```

---

## ✅ Pré-requisitos

- **JDK 17**
- **Maven 3.9+** (ou o wrapper `./mvnw`, se presente)

---

## ▶️ Como executar

```bash
# subir a aplicação (porta 8080)
mvn spring-boot:run

# gerar o .jar
mvn clean package

# rodar os testes
mvn test
```

Ao subir, você verá no log algo como
`Started ChatApplication in X seconds` e `Tomcat started on port 8080`.

**Console do H2** (se habilitado): `http://localhost:8080/h2-console`
JDBC URL `jdbc:h2:file:./data/chatdb`, usuário `sa`, senha em branco.

---

## ⚙️ Configuração (`application.yml`)

| Chave | Default | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta da aplicação. |
| `spring.datasource.url` | `jdbc:h2:file:./data/chatdb` | Banco (troque por PostgreSQL em produção). |
| `spring.jpa.open-in-view` | `false` | Evita lazy-loading fora da camada de serviço. |
| `spring.servlet.multipart.max-file-size` | `10MB` | Limite de upload. |
| `app.cors.allowed-origins` | `http://localhost:5173` | Origem do front-end (Vite). |
| `app.storage.dir` | `./uploads` | Pasta onde os anexos são salvos. |
| `app.storage.max-file-size` | `10485760` | Limite em bytes (10 MB). |

---

## 🌐 API

Base URL: `http://localhost:8080`. Todas as respostas em JSON (UTF-8); o upload usa
`multipart/form-data`.

| Método | Caminho | Descrição | Sucesso |
|---|---|---|---|
| `GET`  | `/api/health` | Health check com checagem de banco. | 200 / 503 |
| `POST` | `/api/conversations` | Cria uma conversa (sessão). | 201 |
| `GET`  | `/api/conversations` | Lista conversas (ordenadas por atividade). | 200 |
| `GET`  | `/api/conversations/{id}/messages` | Histórico completo da sessão. | 200 |
| `POST` | `/api/conversations/{id}/messages` | Envia mensagem e recebe resposta. | 201 |
| `POST` | `/api/conversations/{id}/attachments` | Upload de anexo (`.txt`/`.pdf`, ≤10MB). | 201 |

### Exemplos

**Health**
```bash
curl http://localhost:8080/api/health
```
```json
{ "status": "UP", "service": "chat-backend", "version": "0.1.0",
  "timestamp": "2026-06-25T14:32:00Z", "checks": { "database": "UP" } }
```

**Criar conversa**
```bash
curl -X POST http://localhost:8080/api/conversations \
  -H "Content-Type: application/json" -d "{}"
```
```json
{ "id": "1b9d...", "title": "Nova conversa",
  "createdAt": "2026-06-25T14:32:00Z", "updatedAt": "2026-06-25T14:32:00Z" }
```

**Enviar mensagem** (retorna as duas mensagens persistidas)
```bash
curl -X POST http://localhost:8080/api/conversations/{id}/messages \
  -H "Content-Type: application/json" \
  -d "{\"content\":\"Oi, tudo bem?\"}"
```
```json
{
  "userMessage":      { "id": "...", "role": "USER",      "content": "Oi, tudo bem?", "createdAt": "..." },
  "assistantMessage": { "id": "...", "role": "ASSISTANT", "content": "Recebi sua mensagem: \"Oi, tudo bem?\". [resposta de teste]", "createdAt": "..." }
}
```

**Upload de anexo**
```bash
curl -X POST http://localhost:8080/api/conversations/{id}/attachments \
  -F "file=@./relatorio.pdf"
```

### Envelope de erro

Toda resposta 4xx/5xx segue o mesmo shape:

```json
{
  "timestamp": "2026-06-25T14:32:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Conversa não encontrada: 1b9d...",
  "path": "/api/conversations/1b9d.../messages"
}
```

| Status | Quando |
|---|---|
| `400` | Corpo inválido / `content` vazio (validação). |
| `404` | Conversa não encontrada. |
| `413` | Arquivo acima de 10 MB. |
| `415` | Tipo/extensão de arquivo não suportado. |
| `503` | Health: dependência crítica (banco) indisponível. |

---

## 🤖 Resposta do chat (seam de IA)

Nesta etapa, a resposta do assistente é **determinística**, gerada por
`StubChatResponseProvider`:

```java
public interface ChatResponseProvider {
    String generateReply(String userContent, Conversation context);
}
```

Para plugar um modelo real, basta criar uma nova implementação de
`ChatResponseProvider` e injetá-la no lugar do stub (ex.: `@Primary` ou
`@ConditionalOnProperty`). **Controllers e contrato permanecem intactos.**

---

## 💾 Persistência

Histórico relacional por **ID de sessão** (o ID da conversa). As mensagens
(`USER`/`ASSISTANT`) e os metadados dos anexos são persistidos e recuperáveis pela
sessão. Como o H2 roda em modo arquivo (`./data/chatdb`), os dados **sobrevivem a
reinícios** do servidor.

---

## 🧪 Testes

```bash
mvn test
```

Estratégia de testes do projeto: *slice* de controller com **MockMvc** (status e
shape de cada endpoint) e testes de unidade dos services (regras de domínio, par
USER/ASSISTANT, validação de arquivo).

---

## 🛠️ Geração via IA (SDD)

Este repositório foi construído seguindo **Spec-Driven Development**: o código é um
subproduto das especificações, geradas e implementadas via **opencode**. Os
prompts-base (padrão CRISP), o escopo de atuação do agente e os guardrails estão
documentados em [`AGENTS.md`](./AGENTS.md).

---

## 📋 Endpoints rápidos (resumo)

```
GET    /api/health
POST   /api/conversations
GET    /api/conversations
GET    /api/conversations/{id}/messages
POST   /api/conversations/{id}/messages
POST   /api/conversations/{id}/attachments
```

## Equipe

CAIQUE SIMÕES DE ARAÚJO
DAVI DE SÁ PORTUGAL SILVA
LEONARDO DE MATTOS VEIGA
SIMONE BROMERSCHENCKEL
VANESSA CRISTINA SEVERIANO XAVIER