package com.embabel.stashbot;

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.NavigableDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * Service for managing document ingestion and retrieval.
 */
@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    private final LuceneSearchOperations searchOperations;
    private final TikaHierarchicalContentReader contentReader;

    public DocumentService(LuceneSearchOperations searchOperations) {
        this.searchOperations = searchOperations;
        this.contentReader = new TikaHierarchicalContentReader();
    }

    /**
     * Ingest a file into the RAG store.
     */
    public NavigableDocument ingestFile(File file) {
        logger.info("Ingesting file: {}", file.getName());
        var document = contentReader.parseFile(file, file.toURI().toString());
        searchOperations.writeAndChunkDocument(document);
        logger.info("Ingested file: {}", file.getName());
        return document;
    }

    /**
     * Ingest content from an input stream.
     */
    public NavigableDocument ingestStream(InputStream inputStream, String uri, String filename) {
        logger.info("Ingesting stream: {}", filename);
        var document = contentReader.parseContent(inputStream, uri);
        searchOperations.writeAndChunkDocument(document);
        logger.info("Ingested: {}", filename);
        return document;
    }

    /**
     * Ingest content from a URL.
     */
    public NavigableDocument ingestUrl(String url) {
        logger.info("Ingesting URL: {}", url);
        var document = contentReader.parseResource(url);
        searchOperations.writeAndChunkDocument(document);
        logger.info("Ingested URL: {}", url);
        return document;
    }

    /**
     * Delete a document by its URI.
     */
    public boolean deleteDocument(String uri) {
        logger.info("Deleting document: {}", uri);
        var result = searchOperations.deleteRootAndDescendants(uri);
        return result != null;
    }

    /**
     * Get total document count.
     */
    public int getDocumentCount() {
        return searchOperations.info().getDocumentCount();
    }

    /**
     * Get total chunk count.
     */
    public int getChunkCount() {
        return searchOperations.info().getChunkCount();
    }

    /**
     * Get the underlying search operations for direct access.
     */
    public LuceneSearchOperations getSearchOperations() {
        return searchOperations;
    }
}
