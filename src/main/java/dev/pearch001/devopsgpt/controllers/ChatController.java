package dev.pearch001.devopsgpt.controllers;

// src/main/java/com/devopsgpt/controller/ChatController.java

import dev.pearch001.devopsgpt.model.ChatRequest;
import dev.pearch001.devopsgpt.model.ChatResponse;
import dev.pearch001.devopsgpt.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Pong! DevOpsGPT is running.");
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest chatRequest) {
        String reply = chatService.getChatReply(chatRequest.message());
        return ResponseEntity.ok(new ChatResponse(reply, chatRequest.sessionId()));
    }
}
