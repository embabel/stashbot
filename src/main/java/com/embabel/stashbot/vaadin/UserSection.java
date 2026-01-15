package com.embabel.stashbot.vaadin;

import com.embabel.stashbot.user.StashbotUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * User section component showing avatar, name, and logout button.
 */
class UserSection extends HorizontalLayout {

    UserSection(StashbotUser user) {
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(true);

        // Profile chip with avatar and name
        var profileChip = new HorizontalLayout();
        profileChip.addClassName("profile-chip");
        profileChip.setAlignItems(FlexComponent.Alignment.CENTER);
        profileChip.setSpacing(false);

        // Avatar with initials
        var initials = getInitials(user.getDisplayName());
        var avatar = new Div();
        avatar.setText(initials);
        avatar.addClassName("user-avatar");

        var userName = new Span(user.getDisplayName());
        userName.addClassName("user-name");

        profileChip.add(avatar, userName);

        // Logout button
        var logoutButton = new Button("Logout", e -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
        });
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        logoutButton.addClassName("logout-button");

        add(profileChip, logoutButton);
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        var parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
