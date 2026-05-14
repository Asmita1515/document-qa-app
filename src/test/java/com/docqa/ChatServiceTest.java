package com.docqa;

import com.docqa.model.ChatMessage;
import com.docqa.model.UploadedFile;
import com.docqa.repository.ChatRepository;
import com.docqa.repository.FileRepository;
import com.docqa.service.ChatService;
import com.docqa.service.OpenAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRepository chatRepository;
    @Mock private FileRepository fileRepository;
    @Mock private OpenAIService openAIService;

    @InjectMocks
    private ChatService chatService;

    private UploadedFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new UploadedFile();
        mockFile.setId(1L);
        mockFile.setFileName("test.pdf");
        mockFile.setFileType("pdf");
        mockFile.setExtractedText("This document is about Spring Boot.");
        mockFile.setUserId(1L);
    }

    @Test
    void askQuestion_ShouldReturnSavedChatMessage() {
        when(fileRepository.findById(1L)).thenReturn(Optional.of(mockFile));
        when(openAIService.askQuestion(anyString(), anyString())).thenReturn("Spring Boot is a framework.");
        when(chatRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatService.askQuestion(1L, 1L, "What is this about?");

        assertEquals("What is this about?", result.getQuestion());
        assertEquals("Spring Boot is a framework.", result.getAnswer());
    }

    @Test
    void askQuestion_ShouldThrow_WhenFileNotFound() {
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> chatService.askQuestion(1L, 99L, "question"));
    }

    @Test
    void summarizeFile_ShouldReturnSummary() {
        mockFile.setSummary(null);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(mockFile));
        when(openAIService.summarize(anyString())).thenReturn("This is a summary.");
        when(fileRepository.save(any())).thenReturn(mockFile);

        String summary = chatService.summarizeFile(1L);

        assertEquals("This is a summary.", summary);
    }

    @Test
    void summarizeFile_ShouldReturnCachedSummary_WhenAlreadyExists() {
        mockFile.setSummary("Cached summary.");
        when(fileRepository.findById(1L)).thenReturn(Optional.of(mockFile));

        String summary = chatService.summarizeFile(1L);

        assertEquals("Cached summary.", summary);
        verify(openAIService, never()).summarize(any()); // should NOT call OpenAI again
    }

    @Test
    void getChatHistory_ShouldReturnMessages() {
        List<ChatMessage> msgs = List.of(new ChatMessage(), new ChatMessage());
        when(chatRepository.findByUserIdAndFileIdOrderByCreatedAtAsc(1L, 1L)).thenReturn(msgs);

        List<ChatMessage> result = chatService.getChatHistory(1L, 1L);

        assertEquals(2, result.size());
    }
}
