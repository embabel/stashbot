package com.embabel.stashbot;

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.model.NavigableDocument;
import com.embabel.agent.rag.store.ChunkingContentElementRepository;
import com.embabel.stashbot.user.StashbotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Service for managing document ingestion and retrieval.
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final ChunkingContentElementRepository contentRepository;
    private final TikaHierarchicalContentReader contentReader;

    public record Context(StashbotUser user) {
        public static final String CONTEXT_KEY = "context";
        public Map<String, Object> metadata() {
            return Map.of(
                    "ingestedBy", user.getId(),
                    CONTEXT_KEY, user.getCurrentContext()
            );
        }
    }

    public DocumentService(ChunkingContentElementRepository contentRepository) {
        this.contentRepository = contentRepository;
        this.contentReader = new TikaHierarchicalContentReader();
    }

    /**
     * Ingest a file into the RAG store.
     */
    public NavigableDocument ingestFile(File file, Context context) {
        logger.info("Ingesting file: {}", file.getName());
        var document = contentReader.parseFile(file, file.toURI().toString())
                .withMetadata(context.metadata());
        contentRepository.writeAndChunkDocument(document);
        logger.info("Ingested file: {}", file.getName());
        return document;
    }

    /**
     * Ingest content from an input stream.
     */
    public NavigableDocument ingestStream(InputStream inputStream, String uri, String filename, Context context) {
        logger.info("Ingesting stream: {}", filename);
        var document = contentReader.parseContent(inputStream, uri)
                .withMetadata(context.metadata());
        contentRepository.writeAndChunkDocument(document);
        logger.info("Ingested: {}", filename);
        return document;
    }

    /**
     * Ingest content from a URL.
     */
    public NavigableDocument ingestUrl(String url, Context context) {
        logger.info("Ingesting URL: {}", url);
        var document = contentReader.parseResource(url)
                .withMetadata(context.metadata());
        contentRepository.writeAndChunkDocument(document);
        logger.info("Ingested URL: {}", url);
        return document;
    }

    /**
     * Delete a document by its URI.
     */
    public boolean deleteDocument(String uri) {
        logger.info("Deleting document: {}", uri);
        var result = contentRepository.deleteRootAndDescendants(uri);
        return result != null;
    }

    /**
     * Get total document count.
     */
    public int getDocumentCount() {
        return contentRepository.info().getDocumentCount();
    }

    /**
     * Get total chunk count.
     */
    public int getChunkCount() {
        return contentRepository.info().getChunkCount();
    }

}
