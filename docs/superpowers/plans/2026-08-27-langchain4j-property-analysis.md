# LangChain4j Property Analysis (Phase 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement and thoroughly test the LangChain4j path of the `property-analysis` POC — given a real-estate auction listing URL, fetch the page and use LangChain4j + Google Gemini to extract structured property data, exposed via a REST endpoint and a chat-style Next.js frontend.

**Architecture:** Spring Boot backend with `Controller → PropertyAnalysisService → AIPropertiesAnalisys` (LangChain4j implementation only this phase, dispatched by a `type` param). Page content is fetched server-side via Jsoup before being handed to the LLM. Next.js frontend proxies to the backend and renders results as chat messages.

**Tech Stack:** Java 25, Spring Boot 4.1.1 (Gradle Groovy DSL), LangChain4j 1.19.0 + `langchain4j-google-ai-gemini` 1.19.0, Jsoup 1.23.2, JUnit Jupiter 6.0.3 / AssertJ 3.27.7 / Mockito 5.23.0 (already on the test classpath via the modular `*-test` starters), Next.js 16.3.3 / React 19.2.8 / Tailwind 4.

**Spec:** [docs/superpowers/specs/2026-08-27-langchain4j-property-analysis-poc-design.md](../specs/2026-08-27-langchain4j-property-analysis-poc-design.md)

## Global Constraints

- Backend root: `lang-chain-vs-spring-ai/property-analysis-backend`. Frontend root: `lang-chain-vs-spring-ai/property-analysis-frontend`. Both already scaffolded and committed.
- Base Java package: `br.com.ralvin.propertyanalysis`. Feature code lives under `br.com.ralvin.propertyanalysis.analysis` (exceptions under `br.com.ralvin.propertyanalysis.analysis.exception`).
- LLM provider: Google Gemini Developer API (not Vertex AI) via `dev.langchain4j:langchain4j:1.19.0` and `dev.langchain4j:langchain4j-google-ai-gemini:1.19.0`. Default model name: `gemini-2.5-flash`.
- **Verified imports (do not use older/guessed packages):**
  - `dev.langchain4j.model.chat.ChatModel`
  - `dev.langchain4j.model.chat.Capability` (single value `RESPONSE_FORMAT_JSON_SCHEMA`)
  - `dev.langchain4j.model.googleai.GoogleAiGeminiChatModel` (note: package is `googleai`, not `google`)
  - `dev.langchain4j.service.AiServices`, `dev.langchain4j.service.UserMessage`, `dev.langchain4j.service.V`
  - `dev.langchain4j.model.output.structured.Description`
  - `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` — **moved package in Spring Boot 4.1** (was `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` in older Boot versions). Confirmed by inspecting `spring-boot-webmvc-test-4.1.1.jar`.
  - `org.springframework.test.context.bean.override.mockito.MockitoBean` (replaces the removed `@MockBean`)
  - `org.springframework.test.web.servlet.MockMvc` / `.request.MockMvcRequestBuilders` / `.result.MockMvcResultMatchers` (unchanged package)
- `GEMINI_API_KEY` env var is required for: the app to actually start (`GeminiChatModelConfig` fails placeholder resolution without it), the existing full-context `PropertyAnalysisApplicationTests`, and the real-Gemini `LangChain4jPropertiesAnalysisIntegrationTest`. Both of those tests are gated with `@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")` so `./gradlew test` / `make test` pass (tests SKIPPED, not FAILED) without the key. Every other test in this plan requires no key and no network.
- Endpoint contract: `POST /analysis?type=<lang_chain|spring_ai|rest_client>&link=<url>`. Only `lang_chain` resolves this phase; other types return HTTP 501 via `UnsupportedAnalysisTypeException`.
- `PropertyAnalysisResult` fields (all nullable): `modalidade`, `praca`, `formaPagamento`, `dataLeilao`, `tipoImovel`, `enderecoCompleto` (String); `valorMercado`, `valorArrematacao`, `iptuAnual`, `condominioMensal` (BigDecimal).

---

### Task 1: Domain contract, exceptions

**Files:**
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/PropertyAnalysisResult.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/AIPropertiesAnalisys.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/exception/InvalidAnalysisLinkException.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/exception/PageFetchException.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/exception/PropertyExtractionException.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/exception/UnsupportedAnalysisTypeException.java`

**Interfaces:**
- Produces: `PropertyAnalysisResult` record (10 nullable fields, order: modalidade, praca, formaPagamento, dataLeilao, tipoImovel, enderecoCompleto, valorMercado, valorArrematacao, iptuAnual, condominioMensal) — every later task constructs/consumes it in this exact order. `AIPropertiesAnalisys` interface with `String type()` and `PropertyAnalysisResult analyze(String pageContent)`. Four unchecked exception types.

This task has no behavior to test (pure types) — verification is a successful compile.

- [ ] **Step 1: Create `PropertyAnalysisResult.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.output.structured.Description;

import java.math.BigDecimal;

public record PropertyAnalysisResult(
		@Description("Modalidade do leilão, ex.: Judicial, Extrajudicial")
		String modalidade,

		@Description("Praça do leilão (rodada), ex.: 1ª, 2ª")
		String praca,

		@Description("Forma de pagamento aceita, ex.: À vista, Financiado")
		String formaPagamento,

		@Description("Data em que o leilão ocorre, no formato dd/MM/yyyy")
		String dataLeilao,

		@Description("Tipo do imóvel leiloado, ex.: residencial-apartamento, comercial-sala")
		String tipoImovel,

		@Description("Endereço completo do imóvel")
		String enderecoCompleto,

		@Description("Valor de mercado do imóvel em reais, sem formatação")
		BigDecimal valorMercado,

		@Description("Valor de arrematação (lance mínimo) do imóvel em reais, sem formatação")
		BigDecimal valorArrematacao,

		@Description("Valor do IPTU anual em reais, sem formatação")
		BigDecimal iptuAnual,

		@Description("Valor do condomínio mensal em reais, sem formatação")
		BigDecimal condominioMensal
) {
}
```

- [ ] **Step 2: Create `AIPropertiesAnalisys.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

public interface AIPropertiesAnalisys {

	String type();

	PropertyAnalysisResult analyze(String pageContent);
}
```

- [ ] **Step 3: Create the exception classes**

```java
package br.com.ralvin.propertyanalysis.analysis.exception;

public class InvalidAnalysisLinkException extends RuntimeException {

	public InvalidAnalysisLinkException(String message) {
		super(message);
	}
}
```

```java
package br.com.ralvin.propertyanalysis.analysis.exception;

public class PageFetchException extends RuntimeException {

	public PageFetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
```

```java
package br.com.ralvin.propertyanalysis.analysis.exception;

public class PropertyExtractionException extends RuntimeException {

	public PropertyExtractionException(String message, Throwable cause) {
		super(message, cause);
	}
}
```

```java
package br.com.ralvin.propertyanalysis.analysis.exception;

public class UnsupportedAnalysisTypeException extends RuntimeException {

	public UnsupportedAnalysisTypeException(String message) {
		super(message);
	}
}
```

- [ ] **Step 4: Verify it compiles**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis
git commit -m "feat(property-analysis): add AIPropertiesAnalisys contract and result record"
```

---

### Task 2: PageFetcher (Jsoup) with fixture-based tests

**Files:**
- Modify: `property-analysis-backend/build.gradle`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/PageFetcher.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/JsoupPageFetcher.java`
- Create: `property-analysis-backend/src/test/resources/fixtures/leilao-sample.html`
- Test: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/JsoupPageFetcherTest.java`

**Interfaces:**
- Consumes: `PageFetchException(String, Throwable)` from Task 1.
- Produces: `PageFetcher.fetch(String url): String`, implemented by `JsoupPageFetcher`. `DefaultPropertyAnalysisService` (Task 5) depends on `PageFetcher` by interface.

- [ ] **Step 1: Add `jsoup` dependency to `build.gradle`**

Add this line inside the existing `dependencies { ... }` block, alongside the existing `implementation` lines:

```groovy
	implementation 'org.jsoup:jsoup:1.23.2'
```

- [ ] **Step 2: Create the fixture HTML**

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Leilão de Imóvel - Apartamento Jardim Paulista</title>
</head>
<body>
    <header>
        <nav>Menu de navegação do site de leilões</nav>
    </header>
    <main>
        <h1>Leilão Judicial de Apartamento Residencial</h1>
        <section>
            <p>Modalidade: Judicial</p>
            <p>Praça: 2ª</p>
            <p>Forma de pagamento: À vista</p>
            <p>Data do leilão: 26/04/2024</p>
            <p>Tipo do imóvel: residencial-apartamento</p>
            <p>Endereço completo: Rua Exemplo, 123 - Jardim Paulista, São Paulo - SP</p>
            <p>Valor de mercado: R$ 2.451.000,00</p>
            <p>Valor da arrematação: R$ 1.488.250,00</p>
            <p>IPTU anual: R$ 4.750,00</p>
        </section>
    </main>
    <footer>Rodapé com informações legais do site</footer>
</body>
</html>
```

- [ ] **Step 3: Write the failing test**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsoupPageFetcherTest {

	private HttpServer server;
	private JsoupPageFetcher fetcher;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/leilao-sample.html", exchange -> {
			byte[] body = Files.readAllBytes(Path.of("src/test/resources/fixtures/leilao-sample.html"));
			exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.getResponseBody().close();
		});
		server.createContext("/not-found", exchange -> {
			byte[] body = "not found".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(404, body.length);
			exchange.getResponseBody().write(body);
			exchange.getResponseBody().close();
		});
		server.start();
		fetcher = new JsoupPageFetcher();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void fetchesAndExtractsReadableTextFromPage() {
		String url = "http://localhost:" + server.getAddress().getPort() + "/leilao-sample.html";

		String content = fetcher.fetch(url);

		assertThat(content).contains("Modalidade: Judicial", "Praça: 2ª", "26/04/2024");
	}

	@Test
	void throwsPageFetchExceptionOnNon2xxResponse() {
		String url = "http://localhost:" + server.getAddress().getPort() + "/not-found";

		assertThatThrownBy(() -> fetcher.fetch(url))
				.isInstanceOf(PageFetchException.class);
	}
}
```

- [ ] **Step 4: Run the test to verify it fails**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.JsoupPageFetcherTest"`
Expected: FAIL — `JsoupPageFetcher`/`PageFetcher` do not exist yet.

- [ ] **Step 5: Create `PageFetcher.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

public interface PageFetcher {

	String fetch(String url);
}
```

- [ ] **Step 6: Create `JsoupPageFetcher.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsoupPageFetcher implements PageFetcher {

	private static final String USER_AGENT =
			"Mozilla/5.0 (compatible; PropertyAnalysisBot/1.0; +https://github.com/diegolirio)";
	private static final int TIMEOUT_MILLIS = 10_000;

	@Override
	public String fetch(String url) {
		try {
			Document document = Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.timeout(TIMEOUT_MILLIS)
					.get();
			return document.body().text();
		} catch (IOException e) {
			throw new PageFetchException("Failed to fetch auction page: " + url, e);
		}
	}
}
```

- [ ] **Step 7: Run the test to verify it passes**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.JsoupPageFetcherTest"`
Expected: PASS (2 tests)

- [ ] **Step 8: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/build.gradle \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/PageFetcher.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/JsoupPageFetcher.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/resources/fixtures/leilao-sample.html \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/JsoupPageFetcherTest.java
git commit -m "feat(property-analysis): add Jsoup-based PageFetcher with fixture tests"
```

---

### Task 3: Gemini ChatModel configuration

**Files:**
- Modify: `property-analysis-backend/build.gradle`
- Modify: `property-analysis-backend/src/main/resources/application.properties`
- Modify: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/PropertyAnalysisApplicationTests.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/GeminiChatModelConfig.java`
- Test: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/GeminiChatModelConfigTest.java`

**Interfaces:**
- Produces: a Spring `ChatModel` bean (`dev.langchain4j.model.chat.ChatModel`), consumed by `LangChain4jPropertiesAnalysis` (Task 4) via constructor injection.

- [ ] **Step 1: Add langchain4j dependencies to `build.gradle`**

Add these two lines inside `dependencies { ... }`, alongside the `jsoup` line added in Task 2:

```groovy
	implementation 'dev.langchain4j:langchain4j:1.19.0'
	implementation 'dev.langchain4j:langchain4j-google-ai-gemini:1.19.0'
```

- [ ] **Step 2: Add Gemini properties to `application.properties`**

Append:

```properties
gemini.api-key=${GEMINI_API_KEY}
gemini.model-name=gemini-2.5-flash
```

- [ ] **Step 3: Write the failing test**

```java
package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiChatModelConfigTest {

	@Test
	void buildsChatModelWithJsonSchemaCapabilityEnabled() {
		ChatModel chatModel = new GeminiChatModelConfig()
				.geminiChatModel("fake-key-for-test", "gemini-2.5-flash");

		assertThat(chatModel).isNotNull();
		assertThat(chatModel.supportedCapabilities()).contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA);
	}
}
```

This test never contacts Gemini: `GoogleAiGeminiChatModel.builder().build()` only constructs the client object, it makes no network call.

- [ ] **Step 4: Run the test to verify it fails**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.GeminiChatModelConfigTest"`
Expected: FAIL — `GeminiChatModelConfig` does not exist yet.

- [ ] **Step 5: Create `GeminiChatModelConfig.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiChatModelConfig {

	@Bean
	public ChatModel geminiChatModel(
			@Value("${gemini.api-key}") String apiKey,
			@Value("${gemini.model-name:gemini-2.5-flash}") String modelName) {
		return GoogleAiGeminiChatModel.builder()
				.apiKey(apiKey)
				.modelName(modelName)
				.supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
				.build();
	}
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.GeminiChatModelConfigTest"`
Expected: PASS

- [ ] **Step 7: Gate the full-context test on `GEMINI_API_KEY`**

`PropertyAnalysisApplicationTests` loads the *entire* Spring context, including the `geminiChatModel` bean, which requires `gemini.api-key` to resolve. Without the env var this test would now fail the build. Gate it so it's skipped instead:

Modify `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/PropertyAnalysisApplicationTests.java` to:

```java
package br.com.ralvin.propertyanalysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class PropertyAnalysisApplicationTests {

	@Test
	void contextLoads() {
	}

}
```

- [ ] **Step 8: Run the full test suite to confirm nothing broke**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test`
Expected: `BUILD SUCCESSFUL`. If `GEMINI_API_KEY` is not set in your shell, `PropertyAnalysisApplicationTests.contextLoads` shows as SKIPPED in the report, not FAILED.

- [ ] **Step 9: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/build.gradle \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/resources/application.properties \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/GeminiChatModelConfig.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/GeminiChatModelConfigTest.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/PropertyAnalysisApplicationTests.java
git commit -m "feat(property-analysis): configure Gemini ChatModel bean"
```

---

### Task 4: LangChain4j AI Service + real-Gemini integration test

**Files:**
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/LangChain4jPropertiesAnalysis.java`
- Test: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/LangChain4jPropertiesAnalysisIntegrationTest.java`

**Interfaces:**
- Consumes: `ChatModel` (Task 3), `PropertyAnalysisResult` / `AIPropertiesAnalisys` / `PropertyExtractionException` (Task 1).
- Produces: `LangChain4jPropertiesAnalysis implements AIPropertiesAnalisys`, `type()` returns `"lang_chain"` — this is the exact string `DefaultPropertyAnalysisService` (Task 5) and the controller (Task 6) dispatch on.

This is the task that actually validates LangChain4j's behavior end to end against the real Gemini API — the stated goal of Phase 1. Its test requires a real `GEMINI_API_KEY` to produce a meaningful pass/fail; without the key it is SKIPPED (structurally complete, functionally unverified until a key is available).

- [ ] **Step 1: Write the integration test (will not compile yet — that's expected)**

```java
package br.com.ralvin.propertyanalysis.analysis;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class LangChain4jPropertiesAnalysisIntegrationTest {

	private static LangChain4jPropertiesAnalysis analysis;
	private static String pageContent;

	@BeforeAll
	static void setUp() throws IOException {
		ChatModel geminiChatModel = GoogleAiGeminiChatModel.builder()
				.apiKey(System.getenv("GEMINI_API_KEY"))
				.modelName("gemini-2.5-flash")
				.supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
				.build();
		analysis = new LangChain4jPropertiesAnalysis(geminiChatModel);
		pageContent = Jsoup.parse(new File("src/test/resources/fixtures/leilao-sample.html"), "UTF-8")
				.body().text();
	}

	@Test
	void extractsStructuredDataFromRealGeminiResponse() {
		PropertyAnalysisResult result = analysis.analyze(pageContent);

		assertThat(result.modalidade()).containsIgnoringCase("judicial");
		assertThat(result.dataLeilao()).contains("26/04/2024");
		assertThat(result.tipoImovel()).containsIgnoringCase("apartamento");
		assertThat(result.valorMercado()).isEqualByComparingTo(new BigDecimal("2451000"));
		assertThat(result.valorArrematacao()).isEqualByComparingTo(new BigDecimal("1488250"));
		assertThat(result.iptuAnual()).isEqualByComparingTo(new BigDecimal("4750"));
		assertThat(result.condominioMensal()).isNull();
	}

	@Test
	void typeReturnsLangChainIdentifier() {
		assertThat(analysis.type()).isEqualTo("lang_chain");
	}
}
```

- [ ] **Step 2: Confirm it fails to compile (class under test doesn't exist)**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew compileTestJava`
Expected: FAIL — `cannot find symbol: class LangChain4jPropertiesAnalysis`

- [ ] **Step 3: Create `LangChain4jPropertiesAnalysis.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PropertyExtractionException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Component;

@Component
public class LangChain4jPropertiesAnalysis implements AIPropertiesAnalisys {

	private final PropertyExtractor extractor;

	public LangChain4jPropertiesAnalysis(ChatModel geminiChatModel) {
		this.extractor = AiServices.builder(PropertyExtractor.class)
				.chatModel(geminiChatModel)
				.build();
	}

	@Override
	public String type() {
		return "lang_chain";
	}

	@Override
	public PropertyAnalysisResult analyze(String pageContent) {
		try {
			return extractor.extract(pageContent);
		} catch (RuntimeException e) {
			throw new PropertyExtractionException("Failed to extract property data via LangChain4j", e);
		}
	}

	interface PropertyExtractor {

		@UserMessage("""
				Extraia os dados do leilão de imóvel a partir do conteúdo da página abaixo.
				Se um campo não estiver presente no conteúdo, retorne null para ele.

				Conteúdo da página:
				{{content}}
				""")
		PropertyAnalysisResult extract(@V("content") String content);
	}
}
```

- [ ] **Step 4: Run it — requires `GEMINI_API_KEY`**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && GEMINI_API_KEY=<your-key> ./gradlew test --tests "*.LangChain4jPropertiesAnalysisIntegrationTest"`
Expected: PASS (2 tests). If any numeric or text assertion fails, that's a real finding about LangChain4j + Gemini's extraction behavior — adjust the `@UserMessage` prompt or field `@Description`s (Task 1) and re-run; do not weaken the assertions to make it pass.

Run without the key to confirm the graceful-skip behavior: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.LangChain4jPropertiesAnalysisIntegrationTest"`
Expected: `BUILD SUCCESSFUL`, test shown as SKIPPED.

- [ ] **Step 5: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/LangChain4jPropertiesAnalysis.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/LangChain4jPropertiesAnalysisIntegrationTest.java
git commit -m "feat(property-analysis): implement LangChain4j AI Service extraction"
```

---

### Task 5: PropertyAnalysisService orchestration

**Files:**
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/PropertyAnalysisService.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/DefaultPropertyAnalysisService.java`
- Test: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/DefaultPropertyAnalysisServiceTest.java`

**Interfaces:**
- Consumes: `PageFetcher.fetch(String): String` (Task 2), `AIPropertiesAnalisys.type()`/`.analyze(String)` (Task 1), `InvalidAnalysisLinkException`/`UnsupportedAnalysisTypeException` (Task 1).
- Produces: `PropertyAnalysisService.analyze(String type, String link): PropertyAnalysisResult`, implemented by `DefaultPropertyAnalysisService` — consumed by `AnalysisController` (Task 6).

- [ ] **Step 1: Write the failing test**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPropertyAnalysisServiceTest {

	@Mock
	private PageFetcher pageFetcher;

	private DefaultPropertyAnalysisService service;
	private FakeAnalyzer fakeAnalyzer;

	@BeforeEach
	void setUp() {
		fakeAnalyzer = new FakeAnalyzer();
		service = new DefaultPropertyAnalysisService(pageFetcher, List.of(fakeAnalyzer));
	}

	@Test
	void delegatesToFetcherAndMatchingAnalyzer() {
		when(pageFetcher.fetch("https://example.com/leilao/1")).thenReturn("page text");

		PropertyAnalysisResult result = service.analyze("lang_chain", "https://example.com/leilao/1");

		assertThat(result).isSameAs(fakeAnalyzer.lastResult);
		assertThat(fakeAnalyzer.lastPageContent).isEqualTo("page text");
		verify(pageFetcher).fetch("https://example.com/leilao/1");
	}

	@Test
	void rejectsInvalidLink() {
		assertThatThrownBy(() -> service.analyze("lang_chain", "not-a-url"))
				.isInstanceOf(InvalidAnalysisLinkException.class);
	}

	@Test
	void rejectsUnsupportedType() {
		assertThatThrownBy(() -> service.analyze("spring_ai", "https://example.com/leilao/1"))
				.isInstanceOf(UnsupportedAnalysisTypeException.class);
	}

	private static class FakeAnalyzer implements AIPropertiesAnalisys {

		private String lastPageContent;
		private final PropertyAnalysisResult lastResult = new PropertyAnalysisResult(
				"Judicial", "2ª", "À vista", "26/04/2024", "residencial-apartamento",
				"Rua Exemplo, 123", null, null, null, null);

		@Override
		public String type() {
			return "lang_chain";
		}

		@Override
		public PropertyAnalysisResult analyze(String pageContent) {
			this.lastPageContent = pageContent;
			return lastResult;
		}
	}
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.DefaultPropertyAnalysisServiceTest"`
Expected: FAIL — `DefaultPropertyAnalysisService`/`PropertyAnalysisService` do not exist yet.

- [ ] **Step 3: Create `PropertyAnalysisService.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

public interface PropertyAnalysisService {

	PropertyAnalysisResult analyze(String type, String link);
}
```

- [ ] **Step 4: Create `DefaultPropertyAnalysisService.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultPropertyAnalysisService implements PropertyAnalysisService {

	private final PageFetcher pageFetcher;
	private final Map<String, AIPropertiesAnalisys> analyzersByType;

	public DefaultPropertyAnalysisService(PageFetcher pageFetcher, List<AIPropertiesAnalisys> analyzers) {
		this.pageFetcher = pageFetcher;
		this.analyzersByType = analyzers.stream()
				.collect(Collectors.toMap(AIPropertiesAnalisys::type, analyzer -> analyzer));
	}

	@Override
	public PropertyAnalysisResult analyze(String type, String link) {
		validateLink(link);
		AIPropertiesAnalisys analyzer = analyzersByType.get(type);
		if (analyzer == null) {
			throw new UnsupportedAnalysisTypeException("Unsupported analysis type: " + type);
		}
		String pageContent = pageFetcher.fetch(link);
		return analyzer.analyze(pageContent);
	}

	private void validateLink(String link) {
		URI uri;
		try {
			uri = new URI(link);
		} catch (URISyntaxException e) {
			throw new InvalidAnalysisLinkException("Invalid link: " + link);
		}
		String scheme = uri.getScheme();
		boolean validScheme = "http".equals(scheme) || "https".equals(scheme);
		if (!validScheme || uri.getHost() == null) {
			throw new InvalidAnalysisLinkException("Invalid link: " + link);
		}
	}
}
```

- [ ] **Step 5: Run to verify it passes**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.DefaultPropertyAnalysisServiceTest"`
Expected: PASS (3 tests)

- [ ] **Step 6: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/PropertyAnalysisService.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/DefaultPropertyAnalysisService.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/DefaultPropertyAnalysisServiceTest.java
git commit -m "feat(property-analysis): add PropertyAnalysisService orchestration"
```

---

### Task 6: Controller + exception handling

**Files:**
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/ErrorResponse.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/AnalysisController.java`
- Create: `property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/AnalysisExceptionHandler.java`
- Test: `property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/AnalysisControllerTest.java`

**Interfaces:**
- Consumes: `PropertyAnalysisService.analyze(String, String)` (Task 5), all four exceptions (Task 1).
- Produces: `POST /analysis?type=&link=` — 200 with `PropertyAnalysisResult` JSON body on success; 400/501/502 with `ErrorResponse` JSON body (`{"message": "..."}`) on the respective failures. This is the exact contract the frontend (Task 7) calls.

- [ ] **Step 1: Write the failing test**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PropertyAnalysisService service;

	@Test
	void returnsAnalysisResultOnSuccess() throws Exception {
		PropertyAnalysisResult result = new PropertyAnalysisResult(
				"Judicial", "2ª", "À vista", "26/04/2024", "residencial-apartamento",
				"Rua Exemplo, 123", new BigDecimal("2451000"), new BigDecimal("1488250"),
				new BigDecimal("4750"), null);
		when(service.analyze(eq("lang_chain"), eq("https://example.com/leilao/1"))).thenReturn(result);

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.modalidade").value("Judicial"))
				.andExpect(jsonPath("$.valorMercado").value(2451000));
	}

	@Test
	void returns400OnInvalidLink() throws Exception {
		when(service.analyze(eq("lang_chain"), eq("not-a-url")))
				.thenThrow(new InvalidAnalysisLinkException("Invalid link: not-a-url"));

		mockMvc.perform(post("/analysis")
						.param("type", "lang_chain")
						.param("link", "not-a-url"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Invalid link: not-a-url"));
	}

	@Test
	void returns501OnUnsupportedType() throws Exception {
		when(service.analyze(eq("spring_ai"), eq("https://example.com/leilao/1")))
				.thenThrow(new UnsupportedAnalysisTypeException("Unsupported analysis type: spring_ai"));

		mockMvc.perform(post("/analysis")
						.param("type", "spring_ai")
						.param("link", "https://example.com/leilao/1"))
				.andExpect(status().isNotImplemented());
	}
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.AnalysisControllerTest"`
Expected: FAIL — `AnalysisController` does not exist yet.

- [ ] **Step 3: Create `ErrorResponse.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

public record ErrorResponse(String message) {
}
```

- [ ] **Step 4: Create `AnalysisController.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

	private final PropertyAnalysisService service;

	public AnalysisController(PropertyAnalysisService service) {
		this.service = service;
	}

	@PostMapping("/analysis")
	public PropertyAnalysisResult analyze(@RequestParam String type, @RequestParam String link) {
		return service.analyze(type, link);
	}
}
```

- [ ] **Step 5: Create `AnalysisExceptionHandler.java`**

```java
package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.InvalidAnalysisLinkException;
import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import br.com.ralvin.propertyanalysis.analysis.exception.PropertyExtractionException;
import br.com.ralvin.propertyanalysis.analysis.exception.UnsupportedAnalysisTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AnalysisExceptionHandler {

	@ExceptionHandler(InvalidAnalysisLinkException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidLink(InvalidAnalysisLinkException e) {
		return new ErrorResponse(e.getMessage());
	}

	@ExceptionHandler(PageFetchException.class)
	@ResponseStatus(HttpStatus.BAD_GATEWAY)
	public ErrorResponse handleFetchFailure(PageFetchException e) {
		return new ErrorResponse("Não foi possível acessar a página do leilão.");
	}

	@ExceptionHandler(PropertyExtractionException.class)
	@ResponseStatus(HttpStatus.BAD_GATEWAY)
	public ErrorResponse handleExtractionFailure(PropertyExtractionException e) {
		return new ErrorResponse("Não foi possível extrair os dados do imóvel. Tente novamente.");
	}

	@ExceptionHandler(UnsupportedAnalysisTypeException.class)
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	public ErrorResponse handleUnsupportedType(UnsupportedAnalysisTypeException e) {
		return new ErrorResponse(e.getMessage());
	}
}
```

`@WebMvcTest` auto-includes `@RestControllerAdvice`/`@ControllerAdvice` beans in its component scan (verified against `WebMvcTypeExcludeFilter` in `spring-boot-webmvc-test-4.1.1.jar`), so `AnalysisExceptionHandler` does not need an explicit `@Import` in the test.

- [ ] **Step 6: Run to verify it passes**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test --tests "*.AnalysisControllerTest"`
Expected: PASS (3 tests)

- [ ] **Step 7: Run the full backend suite**

Run: `cd lang-chain-vs-spring-ai/property-analysis-backend && ./gradlew test`
Expected: `BUILD SUCCESSFUL`, all tests pass or (only the two Gemini-gated ones) are skipped.

- [ ] **Step 8: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/ErrorResponse.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/AnalysisController.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/main/java/br/com/ralvin/propertyanalysis/analysis/AnalysisExceptionHandler.java \
        lang-chain-vs-spring-ai/property-analysis-backend/src/test/java/br/com/ralvin/propertyanalysis/analysis/AnalysisControllerTest.java
git commit -m "feat(property-analysis): add AnalysisController with error mapping"
```

---

### Task 7: Frontend chat UI

**Files:**
- Modify: `property-analysis-frontend/next.config.ts`
- Modify: `property-analysis-frontend/src/app/page.tsx`
- Create: `property-analysis-frontend/src/app/property-result-card.tsx`
- Modify: `property-analysis-frontend/src/app/layout.tsx`

**Interfaces:**
- Consumes: `POST /api/analysis?type=lang_chain&link=<url>` (proxied to the backend's `POST /analysis`), success body matching `PropertyAnalysisResult`'s JSON shape (Task 6), error body `{"message": string}`.

No automated frontend tests this phase (per spec) — verified via `npm run build`/`npm run lint` and the manual end-to-end check in Task 8.

- [ ] **Step 1: Add the dev-time proxy rewrite**

Replace the contents of `property-analysis-frontend/next.config.ts`:

```ts
import type { NextConfig } from "next";

const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/analysis",
        destination: `${backendUrl}/analysis`,
      },
    ];
  },
};

export default nextConfig;
```

- [ ] **Step 2: Create the result card component**

```tsx
export type PropertyAnalysisResult = {
  modalidade: string | null;
  praca: string | null;
  formaPagamento: string | null;
  dataLeilao: string | null;
  tipoImovel: string | null;
  enderecoCompleto: string | null;
  valorMercado: number | null;
  valorArrematacao: number | null;
  iptuAnual: number | null;
  condominioMensal: number | null;
};

const FIELD_LABELS: Record<keyof PropertyAnalysisResult, string> = {
  modalidade: "Modalidade",
  praca: "Praça",
  formaPagamento: "Forma de pagamento",
  dataLeilao: "Data do leilão",
  tipoImovel: "Tipo do imóvel",
  enderecoCompleto: "Endereço completo",
  valorMercado: "Valor de mercado",
  valorArrematacao: "Valor de arrematação",
  iptuAnual: "IPTU anual",
  condominioMensal: "Condomínio mensal",
};

export default function PropertyResultCard({
  result,
}: {
  result: PropertyAnalysisResult;
}) {
  const keys = Object.keys(FIELD_LABELS) as Array<keyof PropertyAnalysisResult>;

  return (
    <dl className="rounded-lg bg-zinc-200 px-4 py-3 text-sm dark:bg-zinc-800">
      {keys.map((key) => (
        <div key={key} className="flex justify-between gap-4 py-0.5">
          <dt className="text-zinc-500 dark:text-zinc-400">{FIELD_LABELS[key]}</dt>
          <dd className="text-right font-medium text-zinc-900 dark:text-zinc-50">
            {result[key] ?? "—"}
          </dd>
        </div>
      ))}
    </dl>
  );
}
```

- [ ] **Step 3: Replace the chat page**

Replace the contents of `property-analysis-frontend/src/app/page.tsx`:

```tsx
"use client";

import { FormEvent, useState } from "react";
import PropertyResultCard, {
  PropertyAnalysisResult,
} from "./property-result-card";

type ChatMessage =
  | { role: "user"; id: string; text: string }
  | { role: "assistant"; id: string; result: PropertyAnalysisResult }
  | { role: "assistant-error"; id: string; text: string };

export default function Home() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [link, setLink] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedLink = link.trim();
    if (!trimmedLink || isLoading) {
      return;
    }

    setMessages((prev) => [
      ...prev,
      { role: "user", id: crypto.randomUUID(), text: trimmedLink },
    ]);
    setLink("");
    setIsLoading(true);

    try {
      const response = await fetch(
        `/api/analysis?type=lang_chain&link=${encodeURIComponent(trimmedLink)}`,
        { method: "POST" },
      );

      if (!response.ok) {
        const body = await response
          .json()
          .catch(() => ({ message: "Erro ao analisar o link." }));
        setMessages((prev) => [
          ...prev,
          {
            role: "assistant-error",
            id: crypto.randomUUID(),
            text: body.message ?? "Erro ao analisar o link.",
          },
        ]);
        return;
      }

      const result: PropertyAnalysisResult = await response.json();
      setMessages((prev) => [
        ...prev,
        { role: "assistant", id: crypto.randomUUID(), result },
      ]);
    } catch {
      setMessages((prev) => [
        ...prev,
        {
          role: "assistant-error",
          id: crypto.randomUUID(),
          text: "Falha de conexão com o backend.",
        },
      ]);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="flex flex-1 flex-col items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex w-full max-w-2xl flex-1 flex-col gap-4 px-4 py-8">
        <h1 className="text-xl font-semibold text-black dark:text-zinc-50">
          Análise de Leilão de Imóvel
        </h1>
        <div className="flex flex-1 flex-col gap-3 overflow-y-auto">
          {messages.map((message) => {
            if (message.role === "user") {
              return (
                <div
                  key={message.id}
                  className="max-w-[85%] self-end break-all rounded-lg bg-blue-600 px-4 py-2 text-white"
                >
                  {message.text}
                </div>
              );
            }
            if (message.role === "assistant-error") {
              return (
                <div
                  key={message.id}
                  className="max-w-[85%] self-start rounded-lg bg-red-100 px-4 py-2 text-red-800 dark:bg-red-950 dark:text-red-200"
                >
                  {message.text}
                </div>
              );
            }
            return (
              <div key={message.id} className="max-w-[85%] self-start">
                <PropertyResultCard result={message.result} />
              </div>
            );
          })}
          {isLoading && (
            <div className="self-start rounded-lg bg-zinc-200 px-4 py-2 text-zinc-600 dark:bg-zinc-800 dark:text-zinc-300">
              Analisando…
            </div>
          )}
        </div>
        <form onSubmit={handleSubmit} className="flex gap-2">
          <input
            type="url"
            required
            value={link}
            onChange={(event) => setLink(event.target.value)}
            placeholder="Cole o link do leilão…"
            className="flex-1 rounded-lg border border-zinc-300 px-4 py-2 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-50"
          />
          <button
            type="submit"
            disabled={isLoading}
            className="rounded-lg bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
          >
            Enviar
          </button>
        </form>
      </main>
    </div>
  );
}
```

- [ ] **Step 4: Update the page title**

In `property-analysis-frontend/src/app/layout.tsx`, change:

```ts
export const metadata: Metadata = {
  title: "Create Next App",
  description: "Generated by create next app",
};
```

to:

```ts
export const metadata: Metadata = {
  title: "Análise de Leilão",
  description: "POC LangChain4j — extração de dados de leilão de imóveis via Gemini",
};
```

- [ ] **Step 5: Lint and build the frontend**

Run: `cd lang-chain-vs-spring-ai/property-analysis-frontend && npm run lint && npm run build`
Expected: both succeed with no errors.

- [ ] **Step 6: Commit**

```bash
git add lang-chain-vs-spring-ai/property-analysis-frontend/next.config.ts \
        lang-chain-vs-spring-ai/property-analysis-frontend/src/app/page.tsx \
        lang-chain-vs-spring-ai/property-analysis-frontend/src/app/property-result-card.tsx \
        lang-chain-vs-spring-ai/property-analysis-frontend/src/app/layout.tsx
git commit -m "feat(property-analysis): add chat UI for auction link analysis"
```

---

### Task 8: End-to-end manual verification

**Files:** none (verification only).

This task requires a real `GEMINI_API_KEY` and network access to the target auction site — it is best-effort. If the site blocks the fetch (anti-bot protection), that is a legitimate finding to report, not a plan defect; do not alter application code to work around it without discussing with the user first.

- [ ] **Step 1: Confirm the key is available**

Run: `echo ${GEMINI_API_KEY:+set}`
Expected: `set`. If empty, obtain a key from https://ai.google.dev/gemini-api/docs/api-key, `export GEMINI_API_KEY=...`, and re-run this step.

- [ ] **Step 2: Start both services**

Run (from the repo root): `cd lang-chain-vs-spring-ai && make run`
Expected: backend logs settle on `Started PropertyAnalysisApplication`, frontend logs `Ready` on port 3000.

- [ ] **Step 3: Exercise the chat UI in a browser**

Open `http://localhost:3000`. Paste this link into the input and submit:

```
https://www.leilaoimovel.com.br/imovel/sp/sao-paulo/residencial-apartamento-no-jardim-paulista-imovel-2987093
```

Expected: the link appears as a user message, a loading indicator appears, then either:
- an assistant card with the extracted fields (Modalidade, Praça, Forma de pagamento, Data do leilão, Tipo do imóvel, Endereço completo, Valor de mercado, Valor de arrematação, IPTU anual, Condomínio mensal — some may show `—` if not published on the page), or
- a readable error bubble (not a raw stack trace) if the site couldn't be fetched.

- [ ] **Step 4: Stop the services**

Send `Ctrl+C` to the `make run` process. Confirm ports are free:

Run: `lsof -i:8080; lsof -i:3000`
Expected: no output from either.

- [ ] **Step 5: Report the outcome**

No commit for this task. Summarize what was observed (fields extracted correctly / partially / fetch blocked) back to the user — this is the actual "test well, analyze the code" signal Phase 1 was built to produce.
