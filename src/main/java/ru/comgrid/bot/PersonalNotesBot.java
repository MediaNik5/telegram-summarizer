package ru.comgrid.bot;

import java.sql.SQLException;
import java.util.List;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegram bot implementation.
 */
public class PersonalNotesBot extends TelegramLongPollingBot {
    private final String token;
    private final NoteRepository repository;
    private final EmbeddingService embeddings;
    private final ChatService chat;

    public PersonalNotesBot(String token, NoteRepository repository, EmbeddingService embeddings, ChatService chat) {
        this.token = token;
        this.repository = repository;
        this.embeddings = embeddings;
        this.chat = chat;
    }

    @Override
    public String getBotUsername() {
        return "notes-bot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message == null || !message.hasText()) {
            return;
        }

        String text = message.getText();
        if (text.startsWith("/ask")) {
            handleQuestion(message, text.substring(4).trim());
        } else {
            handleNote(message, text);
        }
    }

    private void handleNote(Message message, String note) {
        try {
            double[] vector = embeddings.embed(note);
            repository.saveNote(note, vector);
            execute(SendMessage.builder().chatId(message.getChatId().toString()).text("Saved").build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleQuestion(Message message, String question) {
        try {
            double[] qVector = embeddings.embed(question);
            List<String> notes = repository.searchSimilar(qVector, 5);
            if (notes.isEmpty()) {
                execute(SendMessage.builder().chatId(message.getChatId().toString()).text("No relevant notes found.").build());
                return;
            }
            String answer = chat.ask(question, notes);
            execute(SendMessage.builder().chatId(message.getChatId().toString()).text(answer).build());
        } catch (Exception e) {
            try {
                execute(SendMessage.builder().chatId(message.getChatId().toString()).text("Error: " + e.getMessage()).build());
            } catch (TelegramApiException ignore) {
            }
        }
    }
}

