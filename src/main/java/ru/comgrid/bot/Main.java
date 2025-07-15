package ru.comgrid.bot;

import com.theokanning.openai.service.OpenAiService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws Exception {
        String telegramToken = System.getenv("TELEGRAM_BOT_TOKEN");
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (telegramToken == null || openaiKey == null || dbUrl == null) {
            System.out.println("Please set TELEGRAM_BOT_TOKEN, OPENAI_API_KEY, DB_URL, DB_USER, DB_PASSWORD");
            return;
        }

        NoteDao dao = new NoteDao(dbUrl, dbUser, dbPassword);
        OpenAiService openai = new OpenAiService(openaiKey);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new NoteBot(telegramToken, dao, openai));
    }
}
