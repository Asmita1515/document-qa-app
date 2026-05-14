package com.docqa.service;

import com.docqa.model.ChatMessage;
import com.docqa.model.UploadedFile;
import com.docqa.repository.ChatRepository;
import com.docqa.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private OpenAIService openAIService;

    /**
     * Ask a question about a file. Returns the AI's answer.
     */
    public ChatMessage askQuestion(Long userId, Long fileId, String question) {
        // 1. Load the file
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        // 2. Check we have extracted text
        String context = file.getExtractedText();
        if (context == null || context.isBlank()) {
            context = "No text content available for this file.";
        }

        // 3. Ask OpenAI
        String answer = openAIService.askQuestion(context, question);

        // 4. Try to find a timestamp (relevant for audio/video)
        Double timestamp = null;
        if ("audio".equals(file.getFileType()) || "video".equals(file.getFileType())) {
            try {
                String tsString = openAIService.findTimestamp(context, question);
                double ts = Double.parseDouble(tsString.trim());
                if (ts >= 0) timestamp = ts;
            } catch (NumberFormatException ignored) { }
        }

        // 5. Save chat history
        ChatMessage chat = new ChatMessage();
        chat.setUserId(userId);
        chat.setFileId(fileId);
        chat.setQuestion(question);
        chat.setAnswer(answer);
        chat.setTimestampSeconds(timestamp);
        chat.setCreatedAt(LocalDateTime.now());

        return chatRepository.save(chat);
    }

    /**
     * Summarize a file. Caches the summary in the DB so we don't call OpenAI twice.
     */
    public String summarizeFile(Long fileId) {
        UploadedFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        // Return cached summary if available
        if (file.getSummary() != null && !file.getSummary().isBlank()) {
            return file.getSummary();
        }

        String text = file.getExtractedText();
        if (text == null || text.isBlank()) {
            return "No content available to summarize.";
        }

        String summary = openAIService.summarize(text);

        // Cache it
        file.setSummary(summary);
        fileRepository.save(file);

        return summary;
    }

    /**
     * Return full chat history for a user + file combination.
     */
    public List<ChatMessage> getChatHistory(Long userId, Long fileId) {
        return chatRepository.findByUserIdAndFileIdOrderByCreatedAtAsc(userId, fileId);
    }
}
