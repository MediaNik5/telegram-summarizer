package ru.comgrid.bot;

import java.sql.Connection;
import java.sql.DriverManager;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Entry point for the personal notes bot.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String dbUrl = System.getenv("DATABASE_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (botToken == null || openaiKey == null || dbUrl == null) {
            System.err.println("Missing configuration. Ensure TELEGRAM_BOT_TOKEN, OPENAI_API_KEY and DATABASE_URL are set.");
            return;
        }

        Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        NoteRepository repo = new NoteRepository(conn);
        EmbeddingService embeddings = new EmbeddingService(openaiKey);
        ChatService chat = new ChatService(openaiKey);

        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(new PersonalNotesBot(botToken, repo, embeddings, chat));
    }
}

