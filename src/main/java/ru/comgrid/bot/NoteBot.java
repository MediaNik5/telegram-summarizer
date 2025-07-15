package ru.comgrid.bot;

import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.Collection;
import tech.amikos.chromadb.Embedding;
import tech.amikos.chromadb.embeddings.openai.OpenAIEmbeddingFunction;
import tech.amikos.chromadb.embeddings.WithParam;
import tech.amikos.chromadb.model.QueryEmbedding;
import java.time.LocalDate;
import java.util.*;

public class NoteBot extends TelegramLongPollingBot {
    private final String token;
    private final String username;
    private final OpenAiService openAi;
    private final Collection collection;
    private LocalDate lastConfirmation = LocalDate.now();
    private int notesSaved = 0;

    public NoteBot(String token, String openAiKey, String chromaUrl) throws Exception {
        this.token = token;
        this.username = Optional.ofNullable(System.getenv("TELEGRAM_BOT_USERNAME")).orElse("notesbot");
        this.openAi = new OpenAiService(openAiKey);
        Client client = new Client(chromaUrl);
        OpenAIEmbeddingFunction ef = new OpenAIEmbeddingFunction(
                WithParam.apiKey(openAiKey),
                WithParam.model("text-embedding-3-small")
        );
        Collection col;
        try {
            col = client.getCollection("notes", ef);
        } catch (Exception e) {
            col = client.createCollection("notes", null, true, ef);
        }
        this.collection = col;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (text.startsWith("/ask")) {
            String question = text.replaceFirst("/ask", "").trim();
            if (!question.isEmpty()) {
                answerQuestion(question, chatId);
            }
        } else if (!text.startsWith("/")) {
            saveNote(text, update.getMessage().getMessageId(), chatId);
        }
    }

    private void saveNote(String note, Integer messageId, Long chatId) {
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model("text-embedding-3-small")
                    .input(List.of(note))
                    .build();
            EmbeddingResult result = openAi.createEmbeddings(request);
            List<Double> vector = result.getData().get(0).getEmbedding();
            List<Float> floatVec = new ArrayList<>();
            for (Double d : vector) {
                floatVec.add(d.floatValue());
            }
            Embedding embedding = Embedding.fromList(floatVec);
            collection.add(List.of(embedding), null, List.of(note), List.of(String.valueOf(messageId)));
            notesSaved++;
            if (!lastConfirmation.isEqual(LocalDate.now())) {
                execute(new SendMessage(String.valueOf(chatId), "✅ " + notesSaved + " notes saved!"));
                notesSaved = 0;
                lastConfirmation = LocalDate.now();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                execute(new SendMessage(String.valueOf(chatId), "Error saving note"));
            } catch (TelegramApiException ignored) {}
        }
    }

    private void answerQuestion(String question, Long chatId) {
        try {
            Collection.QueryResponse qr = collection.query(
                    List.of(question),
                    3,
                    null,
                    null,
                    List.of(QueryEmbedding.IncludeEnum.DOCUMENTS)
            );
            List<List<String>> docs = qr.getDocuments();
            if (docs == null || docs.isEmpty() || docs.get(0).isEmpty()) {
                execute(new SendMessage(String.valueOf(chatId), "Sorry, I don't have any notes on that topic."));
                return;
            }
            StringBuilder context = new StringBuilder();
            for (String doc : docs.get(0)) {
                context.append("- ").append(doc).append("\n");
            }
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a helpful assistant. Answer the user's question based only on the provided context."));
            messages.add(new ChatMessage("user", "Context:\n" + context + "\nQuestion: " + question));
            ChatCompletionRequest req = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();
            ChatCompletionResult res = openAi.createChatCompletion(req);
            String answer = res.getChoices().get(0).getMessage().getContent();
            execute(new SendMessage(String.valueOf(chatId), answer));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                execute(new SendMessage(String.valueOf(chatId), "Error processing your question"));
            } catch (TelegramApiException ignored) {}
        }
    }
}
