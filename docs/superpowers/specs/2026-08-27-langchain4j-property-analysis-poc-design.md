# LangChain4j Property Analysis POC — Design

**Status:** Approved for Phase 1 (LangChain4j only)
**Location:** `lang-chain-vs-spring-ai/property-analysis-backend` (+ `property-analysis-frontend`)

## Purpose

POC comparing LangChain4j vs Spring AI vs a raw `RestClient` baseline for the
same task: given a real-estate auction ("leilão") listing URL, fetch the page
and use an LLM to extract structured property data. Phase 1 implements and
tests **only** the LangChain4j path end to end (backend + chat frontend). The
Spring AI and RestClient implementations are stubbed behind the same
interface so later phases slot in without changing the contract.

Each implementation is free to use its own idiomatic pattern for structured
extraction — the comparison is about developer ergonomics per framework, not
about forcing identical internals:
- **LangChain4j** (this phase): AI Services — a declarative interface whose
  method return type is the target record; LangChain4j builds the schema and
  parses the response.
- **Spring AI** (future phase): `ChatClient.prompt().user(...).call().entity(Type.class)`.
- **RestClient** (future phase): manual HTTP call to the Gemini REST API with
  `generationConfig.responseSchema`, manual Jackson parsing.

## LLM Provider

Google Gemini, via `langchain4j-google-ai-gemini` (Gemini Developer API, API
key auth — not Vertex AI). API key read from env var `GEMINI_API_KEY`. App
fails fast at startup if the key is missing.

`Capability.RESPONSE_FORMAT_JSON_SCHEMA` was deliberately NOT enabled on the
`GoogleAiGeminiChatModel` bean: `langchain4j-google-ai-gemini`'s `SchemaMapper`
never propagates `nullable`, so Gemini's strict schema mode cannot emit null
for absent fields and fabricates values instead — null handling is enforced
via the extraction prompt instead.

## Scaffold

Generated via the `ralvin-new-simple-project` skill/command inside
`lang-chain-vs-spring-ai/`:
- `property-analysis-backend` — Java Spring Boot (Spring Initializr)
- `property-analysis-frontend` — Next.js
- root `Makefile` to build/run both

## Architecture

```
Frontend (chat UI)
   │  POST /analysis?type=lang_chain&link=<url>
   ▼
Controller
   ▼
Service (interface + impl)
   │
   ├─► PageFetcher — fetches the auction page HTML, extracts readable text
   │
   └─► AIPropertiesAnalisys (interface, selected by `type`)
          └─► LangChain4jPropertiesAnalysis (only implementation this phase)
```

`type=spring_ai` and `type=rest_client` return HTTP 501 Not Implemented in
this phase — the interface exists so future phases only add an
implementation class, no contract changes.

## Data Model

```java
public record PropertyAnalysisResult(
    String modalidade,
    String praca,
    String formaPagamento,
    String dataLeilao,
    String tipoImovel,
    String enderecoCompleto,
    BigDecimal valorMercado,
    BigDecimal valorArrematacao,
    BigDecimal iptuAnual,
    BigDecimal condominioMensal
) {}
```

All fields nullable — not every listing publishes every field (e.g.
condomínio mensal is frequently absent).

```java
public interface AIPropertiesAnalisys {
    PropertyAnalysisResult analyze(String pageContent);
}
```

LangChain4j implementation uses an AI Service interface:

```java
interface PropertyExtractor {
    @UserMessage("Extraia os dados do leilão do conteúdo abaixo:\n{{content}}")
    PropertyAnalysisResult extract(@V("content") String pageContent);
}
```

wired via `AiServices.builder(PropertyExtractor.class).chatModel(googleAiGeminiChatModel).build()`,
wrapped by `LangChain4jPropertiesAnalysis implements AIPropertiesAnalisys`.

## Data Flow

**Happy path:** user pastes the auction link as a chat message → frontend
calls `POST /analysis?type=lang_chain&link=<url>` → `Service` calls
`PageFetcher.fetch(link)` to get cleaned page text → `Service` calls
`AIPropertiesAnalisys.analyze(text)` → `PropertyAnalysisResult` returned to
the frontend and rendered as an assistant chat message showing a label/value
list of the extracted fields.

**Error paths, all surfaced as a readable chat message (never a raw stack
trace):**
- Invalid or unreachable URL → HTTP 400.
- Page fetch failure (timeout, anti-bot block, non-2xx) → HTTP 502-class
  response with a message distinct from the extraction-failure message.
- LLM extraction/parsing failure → caught, HTTP 502-class response inviting
  retry.
- Missing `GEMINI_API_KEY` → fails at application startup, not per-request.

## Testing Strategy

- **Unit:** `PageFetcher` tested against a local HTML fixture file (no live
  network); `Service` tested with `AIPropertiesAnalisys` mocked; `Controller`
  tested with `@WebMvcTest` and the service layer mocked.
- **Real-Gemini integration test:** a dedicated test (separate tag, e.g.
  `@Tag("integration")`, excluded from the default test run) that calls the
  real Gemini API through `LangChain4jPropertiesAnalysis`, using the same
  local HTML fixture as input. Runs only when `GEMINI_API_KEY` is present in
  the environment; skipped otherwise. This is the test that actually
  validates LangChain4j's behavior, which is the point of this phase.
- **Frontend:** manual verification in-browser for this phase (chat renders,
  submitting a link shows the extracted fields); no automated frontend tests
  required in Phase 1.

## Frontend (Chat UI)

Minimal chat interface: a message list plus a text input. The user pastes
the auction URL as a message and sends it; the assistant's reply renders the
extracted fields as a label/value list inside a chat bubble. No conversation
memory or streaming in this phase — each submitted link is an independent,
single-shot analysis.

## Explicitly Out of Scope for Phase 1

- Spring AI implementation of `AIPropertiesAnalisys`.
- RestClient (framework-less) implementation of `AIPropertiesAnalisys`.
- Persisted chat history / multi-turn conversation.
- Streaming responses.
- Automated frontend tests.
