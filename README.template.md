# Chat — Backend

API REST em Spring Boot: orquestra conversas, recebe anexos (`.txt`/`.pdf`),
persiste histórico por sessão e expõe `GET /api/health`. Arquitetura em camadas:
**controllers são fronteira HTTP; services detêm o domínio.**

## Stack

Java 17 · Spring Boot 3.5 · Spring Web · Spring Data JPA · H2 (dev) · Bean Validation · Maven

## Pré-requisitos

- JDK 17
- Maven `>= 3.9` (ou o wrapper `./mvnw`)

## Setup & run

```bash
./mvnw spring-boot:run     # sobe em http://localhost:8080
./mvnw clean package       # gera o .jar
./mvnw test                # testes
```

Console do H2 (se habilitado): `http://localhost:8080/h2-console`
(JDBC URL `jdbc:h2:file:./data/chatdb`, user `sa`).

## Configuração (`application.yml`)

| Chave | Default | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta. |
| `spring.datasource.url` | `jdbc:h2:file:./data/chatdb` | Banco (troque por Postgres em prod). |
| `spring.servlet.multipart.max-file-size` | `10MB` | Limite de upload. |
| `app.cors.allowed-origins` | `http://localhost:5173` | Origem do front. |
| `app.storage.dir` | `./uploads` | Pasta dos anexos. |
| `app.storage.max-file-size` | `10485760` | Limite em bytes. |

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| GET  | `/api/health` | Health check (200 UP / 503 DOWN). |
| POST | `/api/conversations` | Cria conversa. |
| GET  | `/api/conversations` | Lista conversas. |
| GET  | `/api/conversations/{id}/messages` | Histórico da sessão. |
| POST | `/api/conversations/{id}/messages` | Envia mensagem, recebe resposta. |
| POST | `/api/conversations/{id}/attachments` | Upload multipart (`.txt`/`.pdf`, ≤10MB). |

Shapes, status e envelope de erro: ver contrato do projeto.

## Estrutura

```
controller/   # fronteira HTTP (sem regra)
service/      # domínio (sem HTTP) — inclui chat/ChatResponseProvider
repository/    # Spring Data JPA
domain/       # entidades + MessageRole
dto/          # request/response (records)
mapper/       # entidade ↔ DTO
config/       # CORS, multipart
exception/    # GlobalExceptionHandler + envelope de erro
```

## Resposta do chat (seam de IA)

Nesta fase, a resposta é **controlada/de teste**, fornecida por
`StubChatResponseProvider`. Trocar por um modelo real = nova implementação de
`ChatResponseProvider` — **sem alterar controllers nem contrato**.

## IA / Geração de código

Artefatos gerados via opencode a partir das specs. Prompts-base e escopo do agente
em [`AGENTS.md`](./AGENTS.md).
