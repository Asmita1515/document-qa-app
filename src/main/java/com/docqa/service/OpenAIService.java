package com.docqa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String askQuestion(String context, String question) {
        String trimmed = context != null && context.length() > 800
                ? context.substring(0, 800) : context;
        String prompt = "Based on this document:\n\n" + trimmed
                + "\n\nAnswer this question briefly: " + question;
        return callGroq(prompt);
    }

    public String summarize(String text) {
        String trimmed = text != null && text.length() > 800
                ? text.substring(0, 800) : text;
        String prompt = "Summarize this document in 3-4 sentences:\n\n" + trimmed;
        return callGroq(prompt);
    }

    public String findTimestamp(String transcript, String topic) {
        return "-1";
    }

    private String callGroq(String userMessage) {
        try {
            // Build JSON body using ObjectMapper (no manual escaping)
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", userMessage);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama-3.1-8b-instant");
            body.put("messages", List.of(message));
            body.put("max_tokens", 300);

            String jsonBody = mapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            // Parse using Jackson
            JsonNode root = mapper.readTree(response.body());
            
            if (root.has("error")) {
                return "Groq error: " + root.get("error").get("message").asText();
            }

            return root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText("No response");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}