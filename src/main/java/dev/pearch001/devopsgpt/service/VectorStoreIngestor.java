package dev.pearch001.devopsgpt.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStoreIngestor {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreIngestor.class);

    private final VectorStore vectorStore;

    @Value("classpath:/documents/*.md")
    private Resource[] documentResources;

    public VectorStoreIngestor(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * This method is executed after the bean has been initialized.
     * It loads the documents, converts them to vectors, and stores them in ChromaDB.
     */
    @PostConstruct
    public void ingestDocuments() throws IOException {
        logger.info("Starting document ingestion process...");

        for (Resource resource : documentResources) {
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> documents = documentReader.get();
            logger.info("Ingesting document: {} with {} parts", resource.getFilename(), documents.size());
            vectorStore.add(documents);
        }

        logger.info("Document ingestion complete.");
    }
}