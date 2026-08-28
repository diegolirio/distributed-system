package br.com.ralvin.propertyanalysis.analysis;

import br.com.ralvin.propertyanalysis.analysis.exception.PageFetchException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class JsoupPageFetcher implements PageFetcher {

	private static final String USER_AGENT =
			"Mozilla/5.0 (compatible; PropertyAnalysisBot/1.0; +https://github.com/diegolirio)";
	private static final int TIMEOUT_MILLIS = 10_000;
	private static final int HARD_DEADLINE_SECONDS = 15;

	@Override
	public String fetch(String url) {
		CompletableFuture<String> future = CompletableFuture.supplyAsync(
				() -> doFetch(url), Executors.newVirtualThreadPerTaskExecutor());
		try {
			return future.get(HARD_DEADLINE_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new PageFetchException("Timed out fetching auction page: " + url, e);
		} catch (ExecutionException e) {
			throw new PageFetchException("Failed to fetch auction page: " + url, e.getCause());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new PageFetchException("Interrupted while fetching auction page: " + url, e);
		}
	}

	private String doFetch(String url) {
		try {
			Document document = Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.timeout(TIMEOUT_MILLIS)
					.get();
			return document.body().text();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
