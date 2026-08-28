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
