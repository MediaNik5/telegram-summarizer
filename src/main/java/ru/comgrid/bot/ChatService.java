package ru.comgrid.bot;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Wrapper around OpenAI chat completions.
 */
public class ChatService {
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public ChatService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String ask(String question, List<String> notes) throws IOException {
        StringBuilder system = new StringBuilder("You answer questions based on the user's notes.\n");
        if (!notes.isEmpty()) {
            system.append("Notes:\n");
            for (String n : notes) {
                system.append("- ").append(n).append("\n");
            }
        }

        String json = mapper.createObjectNode()
                .put("model", "gpt-3.5-turbo")
                .set("messages", mapper.createArrayNode()
                        .add(mapper.createObjectNode().put("role", "system").put("content", system.toString()))
                        .add(mapper.createObjectNode().put("role", "user").put("content", question)))
                .toString();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(json, JSON))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            JsonNode root = mapper.readTree(response.body().string());
            return root.get("choices").get(0).get("message").get("content").asText();
        }
    }
}

