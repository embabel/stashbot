package com.embabel.stashbot.user;

import com.embabel.agent.api.identity.User;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * User model for Stashbot.
 */
public class StashbotUser implements User {

    private final String id;
    private final String displayName;
    private final String username;

    private List<String> contexts = new LinkedList<>();

    private String currentContext;

    public StashbotUser(String id, String displayName, String username) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
        this.contexts.addAll(List.of("work", "personal"));
        this.currentContext = "personal";
    }

    public List<String> getContexts() {
        return contexts;
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String currentContext) {
        if (!contexts.contains(currentContext)) {
            contexts.add(currentContext);
        }
        this.currentContext = currentContext;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    @Nullable
    public String getEmail() {
        return null;
    }
}
