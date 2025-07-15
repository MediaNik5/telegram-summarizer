package ru.comgrid.bot;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.List;

public class NoteBot extends TelegramLongPollingBot {
    private final String token;
    private final NoteDao dao;
    private final OpenAiService openai;

    public NoteBot(String token, NoteDao dao, OpenAiService openai) {
        this.token = token;
        this.dao = dao;
        this.openai = openai;
    }

    @Override
    public String getBotUsername() {
        return "NotesBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (text.startsWith("/start")) {
                sendMessage(chatId, "Hello! Send me notes and use /ask <question> to query them.");
            } else if (text.startsWith("/ask")) {
                String question = text.replaceFirst("/ask", "").trim();
                handleQuestion(chatId, question);
            } else {
                handleNote(chatId, text);
            }
        }
    }

    private void handleNote(long chatId, String note) {
        try {
            float[] embedding = embed(note);
            dao.insert(note, embedding);
            sendMessage(chatId, "Note saved.");
        } catch (Exception e) {
            sendMessage(chatId, "Failed to save note.");
        }
    }

    private void handleQuestion(long chatId, String question) {
        try {
            float[] queryEmbedding = embed(question);
            List<String> notes = dao.search(queryEmbedding, 5);
            if (notes.isEmpty()) {
                sendMessage(chatId, "I couldn't find relevant notes.");
                return;
            }
            StringBuilder context = new StringBuilder();
            for (String n : notes) {
                context.append("- ").append(n).append("\n");
            }
            String answer = ask(question, context.toString());
            sendMessage(chatId, answer);
        } catch (Exception e) {
            sendMessage(chatId, "Failed to answer your question.");
        }
    }

    private float[] embed(String text) {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(List.of(text))
                .build();
        List<Embedding> embeddings = openai.createEmbeddings(request).getData();
        List<Double> vector = embeddings.get(0).getEmbedding();
        float[] arr = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            arr[i] = vector.get(i).floatValue();
        }
        return arr;
    }

    private String ask(String question, String context) {
        ChatMessage system = new ChatMessage(ChatMessageRole.SYSTEM.value(), "Use the notes to answer the question.");
        ChatMessage user = new ChatMessage(ChatMessageRole.USER.value(), context + "\nQuestion: " + question);
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(system, user))
                .build();
        ChatCompletionChoice choice = openai.createChatCompletion(request).getChoices().get(0);
        return choice.getMessage().getContent();
    }

    private void sendMessage(long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            // ignore
        }
    }
}
