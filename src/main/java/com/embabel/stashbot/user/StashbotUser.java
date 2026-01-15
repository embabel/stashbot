package com.embabel.stashbot.user;

import com.embabel.agent.api.identity.User;
import org.jetbrains.annotations.Nullable;

/**
 * User model for Stashbot.
 */
public class StashbotUser implements User {

    private final String id;
    private final String displayName;
    private final String username;

    public StashbotUser(String id, String displayName, String username) {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
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
