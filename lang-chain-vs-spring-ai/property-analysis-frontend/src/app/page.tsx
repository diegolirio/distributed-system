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
