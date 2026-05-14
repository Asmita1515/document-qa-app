package com.docqa.controller;

import com.docqa.model.ChatMessage;
import com.docqa.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * POST /api/chat/ask
     * Body: { "userId": 1, "fileId": 1, "question": "What is this document about?" }
     * Returns: { "question": "...", "answer": "...", "timestampSeconds": null }
     */
    @PostMapping("/ask")
    public ResponseEntity<?> askQuestion(@RequestBody Map<String, Object> body) {
        try {
            Long userId  = Long.valueOf(body.get("userId").toString());
            Long fileId  = Long.valueOf(body.get("fileId").toString());
            String question = (String) body.get("question");

            if (question == null || question.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question cannot be empty."));
            }

            ChatMessage result = chatService.askQuestion(userId, fileId, question);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/chat/summarize/{fileId}
     * Returns: { "summary": "..." }
     */
    @PostMapping("/summarize/{fileId}")
    public ResponseEntity<?> summarize(@PathVariable Long fileId) {
        try {
            String summary = chatService.summarizeFile(fileId);
            return ResponseEntity.ok(Map.of("summary", summary));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/chat/history?userId=1&fileId=1
     * Returns all past questions and answers for a file.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam Long userId,
            @RequestParam Long fileId) {
        List<ChatMessage> history = chatService.getChatHistory(userId, fileId);
        return ResponseEntity.ok(history);
    }
}
