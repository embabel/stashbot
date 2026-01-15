package com.embabel.stashbot;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.EmbabelComponent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.rag.filter.PropertyFilter;
import com.embabel.agent.rag.service.SearchOperations;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;
import com.embabel.stashbot.user.StashbotUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The platform can use any action to respond to user messages.
 */
@EmbabelComponent
public class ChatActions {

    private final Logger logger = LoggerFactory.getLogger(ChatActions.class);

    private final SearchOperations searchOperations;
    private final StashbotProperties properties;

    public ChatActions(
            SearchOperations searchOperations,
            StashbotProperties properties) {
        this.searchOperations = searchOperations;
        this.properties = properties;
    }

    /**
     * Bind user to AgentProcess. Will run once at the start of the process.
     * Also record that there has been a null last analysis
     */
    @Action
    StashbotUser bindUser(OperationContext context) {
        var forUser = context.getProcessContext().getProcessOptions().getIdentities().getForUser();
        if (forUser instanceof StashbotUser su) {
            return su;
        } else {
            logger.warn("bindUser: forUser is not an StashbotUser: {}", forUser);
            return null;
        }
    }

    @Action(
            canRerun = true,
            trigger = UserMessage.class
    )
    void respond(
            Conversation conversation,
            StashbotUser user,
            ActionContext context) {
        // We create the instance of ToolishRag just in time
        // to limit results to the user's current context
        var toolishRag = new ToolishRag(
                "docs",
                "Document knowledge base",
                searchOperations)
                .withMetadataFilter(new PropertyFilter.Eq(
                        DocumentService.Context.CONTEXT_KEY,
                        user.getCurrentContext()
                ));
        var assistantMessage = context.
                ai()
                .withLlm(properties.chatLlm())
                .withReference(toolishRag)
                .withTemplate("stashbot")
                .respondWithSystemPrompt(conversation, Map.of(
                        "properties", properties
                ));
        context.sendMessage(conversation.addMessage(assistantMessage));
    }
}
