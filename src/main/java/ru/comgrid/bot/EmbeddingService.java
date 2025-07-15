package ru.comgrid.bot;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Wrapper around the OpenAI embeddings API.
 */
public class EmbeddingService {
    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public EmbeddingService(String apiKey) {
        this.apiKey = apiKey;
    }

    public double[] embed(String text) throws IOException {
        String body = String.format("{\"model\":\"text-embedding-3-small\",\"input\":%s}", mapper.writeValueAsString(text));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .post(RequestBody.create(body, JSON))
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }
            JsonNode json = mapper.readTree(response.body().string());
            JsonNode arr = json.get("data").get(0).get("embedding");
            double[] vec = new double[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                vec[i] = arr.get(i).asDouble();
            }
            return vec;
        }
    }
}

