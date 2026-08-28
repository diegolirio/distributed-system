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
