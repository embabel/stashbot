package com.embabel.stashbot.user;

import com.embabel.agent.api.identity.UserService;

/**
 * Service for managing Stashbot users.
 */
public interface StashbotUserService extends UserService<StashbotUser> {

    /**
     * Get the currently authenticated user.
     */
    StashbotUser getAuthenticatedUser();
}
