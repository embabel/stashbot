package com.embabel.stashbot.vaadin;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Chat message bubble component with sender name and text content.
 * Styled differently for user vs assistant messages.
 * Assistant messages render markdown as HTML.
 */
public class ChatMessageBubble extends Div {

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

    public ChatMessageBubble(String sender, String text, boolean isUser) {
        addClassName("chat-bubble-container");
        addClassName(isUser ? "user" : "assistant");

        var messageDiv = new Div();
        messageDiv.addClassName("chat-bubble");
        messageDiv.addClassName(isUser ? "user" : "assistant");

        var senderSpan = new Span(sender);
        senderSpan.addClassName("chat-bubble-sender");

        if (isUser) {
            var textSpan = new Span(text);
            textSpan.addClassName("chat-bubble-text");
            messageDiv.add(senderSpan, textSpan);
        } else {
            var contentDiv = new Div();
            contentDiv.addClassName("chat-bubble-text");
            contentDiv.add(new Html("<div>" + renderMarkdown(text) + "</div>"));
            messageDiv.add(senderSpan, contentDiv);
        }

        add(messageDiv);
    }

    private static String renderMarkdown(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        var document = MARKDOWN_PARSER.parse(markdown.strip());
        return HTML_RENDERER.render(document).strip();
    }

    public static ChatMessageBubble user(String text) {
        return new ChatMessageBubble("You", text, true);
    }

    public static ChatMessageBubble assistant(String persona, String text) {
        return new ChatMessageBubble(persona, text, false);
    }

    public static Div error(String text) {
        var messageDiv = new Div();
        messageDiv.addClassName("chat-bubble-error");
        messageDiv.setText(text);
        return messageDiv;
    }
}
