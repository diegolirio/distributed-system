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
