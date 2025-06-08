package dev.pearch001.devopsgpt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    private final VectorStore vectorStore; // Inject the VectorStore



    private static final String RAG_PROMPT_TEMPLATE = """
        You are DevOpsGPT, a helpful AI assistant. Your user is asking a question about DevOps or Cloud topics.
        Use the information from the "DOCUMENTS" section to provide a detailed and accurate answer.
        If the documents do not contain the answer, state that you don't have enough information from the knowledge base.
        Do not make up information.

        DOCUMENTS:
        {documents}

        USER'S QUESTION:
        {input}
        """;


    // A simple system prompt to define the assistant's persona
    private static final String SYSTEM_PROMPT = """
        You are DevOpsGPT, a world-class AI assistant specializing in DevOps, cloud computing, and software engineering. 
        Your responses should be accurate, concise, and helpful.
        """;

    /**
     * Spring AI's ChatClient is automatically configured and injected.
     * @param chatClientBuilder The builder used to create a ChatClient instance.
     */
    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
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

    /**
     * Generates a reply using RAG.
     */
    public String getRagReply(String userMessage) {
        logger.info("Received RAG chat request: '{}'", userMessage);

        // 1. Retrieve relevant documents from the vector store
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userMessage) // Set the query
                        .topK(3)            // Retrieve top 3 documents
                        .build()            // Build the SearchRequest
        );

        // Log the source documents used, fulfilling the deliverable
        List<String> sourceDocuments = similarDocuments.stream()
                .map(doc -> doc.getMetadata().get("source").toString())
                .distinct()
                .collect(Collectors.toList());
        logger.info("Found {} relevant document sources: {}", sourceDocuments.size(), sourceDocuments);

        String documentsText = similarDocuments.stream()
                .map(Document::getText) // Replace with the correct method
                .collect(Collectors.joining("\n---\n"));

        // 2. Create a prompt with the retrieved documents and user question
        PromptTemplate promptTemplate = new PromptTemplate(RAG_PROMPT_TEMPLATE);
        Prompt prompt = promptTemplate.create(Map.of(
                "documents", documentsText,
                "input", userMessage
        ));

        // 3. Send the enhanced prompt to the LLM
        ChatResponse response = (ChatResponse) chatClient.prompt(prompt).call();
        return response.getResult().getOutput().getText();
    }
}