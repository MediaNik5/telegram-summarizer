package ru.comgrid.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws Exception {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        String openAiKey = System.getenv("OPENAI_API_KEY");
        String chromaUrl = System.getenv("CHROMA_URL");
        if (token == null || openAiKey == null || chromaUrl == null) {
            System.err.println("Please set TELEGRAM_BOT_TOKEN, OPENAI_API_KEY and CHROMA_URL environment variables.");
            return;
        }
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new NoteBot(token, openAiKey, chromaUrl));
    }
}
