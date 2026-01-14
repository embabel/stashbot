package com.embabel.stashbot;

import com.embabel.agent.rag.ingestion.transform.AddTitlesChunkTransformer;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.common.ai.model.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
@EnableConfigurationProperties(StashbotProperties.class)
class RagConfiguration {

    private final Logger logger = LoggerFactory.getLogger(RagConfiguration.class);

    @Bean
    LuceneSearchOperations luceneSearchOperations(
            ModelProvider modelProvider,
            StashbotProperties properties) {
        var embeddingService = modelProvider.getEmbeddingService(DefaultModelSelectionCriteria.INSTANCE);
        var luceneSearchOperations = LuceneSearchOperations
                .withName("docs")
                .withEmbeddingService(embeddingService)
                .withChunkerConfig(properties.chunkerConfig())
                .withChunkTransformer(AddTitlesChunkTransformer.INSTANCE)
                .withIndexPath(Paths.get("./.lucene-index"))
                .buildAndLoadChunks();
        logger.info("Loaded {} chunks into Lucene RAG store", luceneSearchOperations.info().getChunkCount());
        return luceneSearchOperations;
    }

}
