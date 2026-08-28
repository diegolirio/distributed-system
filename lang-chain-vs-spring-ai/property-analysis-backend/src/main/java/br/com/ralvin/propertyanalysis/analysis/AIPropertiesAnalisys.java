package br.com.ralvin.propertyanalysis.analysis;

public interface AIPropertiesAnalisys {

	String type();

	PropertyAnalysisResult analyze(String pageContent);
}
