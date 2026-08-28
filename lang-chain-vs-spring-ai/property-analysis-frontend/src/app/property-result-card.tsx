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
