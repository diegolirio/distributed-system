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

		@Description("Valor do condomínio mensal em reais, sem formatação. Se não informado no conteúdo, retorne null (nunca -1 ou 0)")
		BigDecimal condominioMensal
) {
}
