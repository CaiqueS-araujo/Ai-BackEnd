# 01 — CONTRATO DA API (`v0.1.0`)

> **Fonte única da verdade.** Front e back implementam exatamente o que está
> aqui. Qualquer divergência entre as camadas é um bug de contrato, não de
> implementação. Mudou aqui → atualiza os dois lados.

---

## 1. Convenções globais

| Aspecto | Regra |
|---|---|
| Base URL | Configurável. Front lê de `VITE_API_BASE_URL`; back sobe em `http://localhost:8080`. |
| Prefixo | Todos os endpoints sob `/api`. |
| Formato | `application/json` (UTF-8). Exceção: upload de anexo usa `multipart/form-data`. |
| IDs | `UUID` v4 em string. O **ID da conversa é o ID de sessão**. |
| Datas | ISO-8601 em UTC, ex.: `2026-06-25T14:32:00Z`. |
| Idempotência | Não exigida nesta etapa. |
| Auth | Não há nesta etapa (endpoints públicos). |

### CORS

Como front e back vivem em origens diferentes, o back **deve** habilitar CORS
para a origem do front (configurável; default `http://localhost:5173`), métodos
`GET, POST, OPTIONS` e headers padrão + `Content-Type`.

---

## 2. Envelope de erro (padrão único)

Toda resposta de erro (4xx/5xx) segue **este shape**:

```json
{
  "timestamp": "2026-06-25T14:32:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Conversa não encontrada: 1b9d...",
  "path": "/api/conversations/1b9d.../messages"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `timestamp` | string (ISO-8601) | Momento do erro. |
| `status` | number | Código HTTP. |
| `error` | string | Nome curto do status. |
| `message` | string | Mensagem legível (pt-BR). |
| `path` | string | Caminho que originou o erro. |

---

## 3. Modelos (DTOs)

### `Role`
```
"USER" | "ASSISTANT"
```

### `Message`
```json
{
  "id": "uuid",
  "conversationId": "uuid",
  "role": "USER",
  "content": "string",
  "createdAt": "ISO-8601"
}
```

### `Conversation` (detalhe)
```json
{
  "id": "uuid",
  "title": "string",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

### `ConversationSummary` (item de listagem)
```json
{
  "id": "uuid",
  "title": "string",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601",
  "messageCount": 4,
  "lastMessagePreview": "string | null"
}
```

### `Attachment`
```json
{
  "id": "uuid",
  "conversationId": "uuid",
  "filename": "relatorio.pdf",
  "contentType": "application/pdf",
  "sizeBytes": 84213,
  "createdAt": "ISO-8601"
}
```

### `Health`
```json
{
  "status": "UP",
  "service": "chat-backend",
  "version": "0.1.0",
  "timestamp": "ISO-8601",
  "checks": { "database": "UP" }
}
```

---

## 4. Endpoints

### 4.1 Health — `GET /api/health`

Verificação de integridade. Base estável para monitoramento futuro.

- **200** → `Health` com `status: "UP"`.
- **503** → `Health` com `status: "DOWN"` se uma dependência crítica (ex.: banco) falhar.

```http
GET /api/health
```
```json
200 OK
{ "status": "UP", "service": "chat-backend", "version": "0.1.0",
  "timestamp": "2026-06-25T14:32:00Z", "checks": { "database": "UP" } }
```

---

### 4.2 Criar conversa — `POST /api/conversations`

Inicia uma nova sessão.

**Request** (body opcional):
```json
{ "title": "string (opcional)" }
```
Se `title` ausente/vazio → o servidor gera `"Nova conversa"`.

- **201** → `Conversation`.
- **400** → corpo malformado.

```json
201 Created
{ "id": "1b9d...", "title": "Nova conversa",
  "createdAt": "2026-06-25T14:32:00Z", "updatedAt": "2026-06-25T14:32:00Z" }
```

---

### 4.3 Listar conversas — `GET /api/conversations`

- **200** → `ConversationSummary[]`, ordenado por `updatedAt` **desc**.

```json
200 OK
[
  { "id": "1b9d...", "title": "Dúvida sobre frete", "createdAt": "...",
    "updatedAt": "...", "messageCount": 6, "lastMessagePreview": "Obrigado!" }
]
```

---

### 4.4 Histórico — `GET /api/conversations/{conversationId}/messages`

Recupera o histórico **completo** da sessão, em ordem cronológica **asc**.

- **200** → `Message[]`.
- **404** → conversa não encontrada.

---

### 4.5 Enviar mensagem — `POST /api/conversations/{conversationId}/messages`

Fluxo HTTP **síncrono**: o cliente envia o texto, o servidor persiste a mensagem
do usuário, gera a resposta (controlada/de teste nesta fase) e persiste a resposta.

**Request**:
```json
{ "content": "string (obrigatório, não-vazio)" }
```

**Response** (retorna as **duas** mensagens já persistidas, com IDs do servidor):
```json
201 Created
{
  "userMessage":      { "id": "...", "conversationId": "...", "role": "USER",      "content": "Oi", "createdAt": "..." },
  "assistantMessage": { "id": "...", "conversationId": "...", "role": "ASSISTANT", "content": "Recebi sua mensagem: \"Oi\". [resposta de teste]", "createdAt": "..." }
}
```

- **400** → `content` vazio/ausente.
- **404** → conversa não encontrada.

> **Seam de IA:** `assistantMessage.content` vem de um provedor de resposta
> (`ChatResponseProvider`). Nesta fase é um stub determinístico. A troca por um
> modelo real **não altera este contrato**.

---

### 4.6 Upload de anexo — `POST /api/conversations/{conversationId}/attachments`

`Content-Type: multipart/form-data`, campo do arquivo: **`file`**.

**Validações (obrigatórias):**

| Regra | Valor |
|---|---|
| Tipos aceitos | `text/plain`, `application/pdf` |
| Extensões aceitas | `.txt`, `.pdf` |
| Tamanho máximo | `10 MB` (configurável) |

- **201** → `Attachment` (metadados registrados).
- **400** → nenhum arquivo enviado / arquivo vazio.
- **404** → conversa não encontrada.
- **413** → arquivo acima do limite.
- **415** → tipo/extensão não suportado.

```json
201 Created
{ "id": "...", "conversationId": "...", "filename": "relatorio.pdf",
  "contentType": "application/pdf", "sizeBytes": 84213, "createdAt": "..." }
```

---

## 5. Tabela-resumo

| Método | Caminho | Sucesso | Erros |
|---|---|---|---|
| GET  | `/api/health` | 200 | 503 |
| POST | `/api/conversations` | 201 | 400 |
| GET  | `/api/conversations` | 200 | — |
| GET  | `/api/conversations/{id}/messages` | 200 | 404 |
| POST | `/api/conversations/{id}/messages` | 201 | 400, 404 |
| POST | `/api/conversations/{id}/attachments` | 201 | 400, 404, 413, 415 |

---

## 6. Espelho TypeScript (para o front)

Estes tipos devem viver em `src/api/contracts.ts` e ser o espelho exato deste documento.

```ts
export type Role = "USER" | "ASSISTANT";

export interface Message {
  id: string;
  conversationId: string;
  role: Role;
  content: string;
  createdAt: string;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConversationSummary extends Conversation {
  messageCount: number;
  lastMessagePreview: string | null;
}

export interface Attachment {
  id: string;
  conversationId: string;
  filename: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
}

export interface SendMessageRequest { content: string; }
export interface SendMessageResponse { userMessage: Message; assistantMessage: Message; }
export interface CreateConversationRequest { title?: string; }

export interface Health {
  status: "UP" | "DOWN";
  service: string;
  version: string;
  timestamp: string;
  checks: Record<string, "UP" | "DOWN">;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
```
