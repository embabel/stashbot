package com.embabel.stashbot;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.common.ai.model.LlmOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Properties for chatbot
 *
 * @param chatLlm       LLM model and hyperparameters to use
 * @param objective     the goal of the chatbot's responses
 * @param voice         the persona and output style of the chatbot
 * @param chunkerConfig configuration for ingestion
 */
@ConfigurationProperties(prefix = "stashbot")
public record StashbotProperties(
        @NestedConfigurationProperty LlmOptions chatLlm,
        String objective,
        @NestedConfigurationProperty Voice voice,
        @NestedConfigurationProperty ContentChunker.Config chunkerConfig
) {

    public record Voice(
            String persona,
            int maxWords
    ) {
    }
}
