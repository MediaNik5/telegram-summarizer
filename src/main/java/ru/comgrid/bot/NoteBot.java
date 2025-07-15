package ru.comgrid.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;

import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.embeddings.EmbeddingFunction;
import tech.amikos.chromadb.embeddings.openai.OpenAIEmbeddingFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class NoteBot extends TelegramLongPollingBot {
    private final String token;
    private final String username;
    private final OpenAiService openAi;
    private final Collection collection;
    private Instant lastConfirm = Instant.now();
    private int notesCount = 0;

    public NoteBot(String token, String username, String openAiKey, String chromaUrl) throws Exception {
        this.token = token;
        this.username = username;
        this.openAi = new OpenAiService(openAiKey);

        Client client = new Client(chromaUrl);
        EmbeddingFunction ef = new OpenAIEmbeddingFunction(
                tech.amikos.chromadb.embeddings.WithParam.apiKey(openAiKey),
                tech.amikos.chromadb.embeddings.WithParam.model("text-embedding-3-small")
        );
        Collection temp;
        try {
            temp = client.getCollection("notes", ef);
        } catch (Exception e) {
            temp = client.createCollection("notes", null, true, ef);
        }
        this.collection = temp;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText().trim();
        if (text.startsWith("/ask")) {
            String question = text.replaceFirst("/ask\\s*", "");
            handleQuestion(message, question);
        } else if (!text.startsWith("/")) {
            saveNote(message);
        }
    }

    private void saveNote(Message message) {
        try {
            collection.add(null, null, Collections.singletonList(message.getText()),
                    Collections.singletonList(String.valueOf(message.getMessageId())));
            notesCount++;
            if (Duration.between(lastConfirm, Instant.now()).toHours() >= 24) {
                execute(SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text("✅ " + notesCount + " notes saved!")
                        .build());
                lastConfirm = Instant.now();
                notesCount = 0;
            }
        } catch (Exception e) {
            sendText(message.getChatId().toString(), "Failed to save note.");
        }
    }

    private void handleQuestion(Message message, String question) {
        try {
            Collection.QueryResponse res = collection.query(Collections.singletonList(question), 3, null, null, null);
            List<List<String>> docs = res.getDocuments();
            String context = "";
            if (docs != null && !docs.isEmpty()) {
                context = String.join("\n", docs.get(0));
            }
            if (context.isBlank()) {
                sendText(message.getChatId().toString(), "Sorry, I don't have any notes on that topic.");
                return;
            }

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a helpful assistant. Answer the user's question based only on the provided context."));
            messages.add(new ChatMessage("system", "Context:\n" + context));
            messages.add(new ChatMessage("user", question));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            ChatCompletionResult result = openAi.createChatCompletion(request);
            String answer = result.getChoices().get(0).getMessage().getContent().trim();
            sendText(message.getChatId().toString(), answer);
        } catch (Exception e) {
            sendText(message.getChatId().toString(), "Error processing your request.");
        }
    }

    private void sendText(String chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (Exception ignored) {
        }
    }

    public static void start() throws Exception {
        String token = System.getenv("TELEGRAM_BOT_TOKEN");
        String username = Optional.ofNullable(System.getenv("TELEGRAM_BOT_USERNAME")).orElse("NoteBot");
        String openAiKey = System.getenv("OPENAI_API_KEY");
        String chromaUrl = Optional.ofNullable(System.getenv("CHROMA_URL")).orElse("http://localhost:8000");
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new NoteBot(token, username, openAiKey, chromaUrl));
    }
}
