package dev.pearch001.devopsgpt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    // A simple system prompt to define the assistant's persona
    private static final String SYSTEM_PROMPT = """
        You are DevOpsGPT, a world-class AI assistant specializing in DevOps, cloud computing, and software engineering. 
        Your responses should be accurate, concise, and helpful.
        """;

    /**
     * Spring AI's ChatClient is automatically configured and injected.
     * @param chatClientBuilder The builder used to create a ChatClient instance.
     */
    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Generates a reply using the injected Spring AI ChatClient.
     * @param userMessage The message from the user.
     * @return The AI-generated response.
     */
    public String getChatReply(String userMessage) {
        logger.info("Processing chat message using Spring AI: '{}'", userMessage);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT) // Set the system persona for the chat
                .user(userMessage)   // Provide the user's message
                .call()              // Execute the call to the LLM
                .content();          // Extract the string content from the response
    }
}