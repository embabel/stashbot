package com.embabel.stashbot.vaadin;

import com.embabel.agent.api.channel.MessageOutputChannelEvent;
import com.embabel.agent.api.channel.OutputChannel;
import com.embabel.agent.api.channel.OutputChannelEvent;
import com.embabel.agent.api.channel.ProgressOutputChannelEvent;
import com.embabel.chat.*;
import com.embabel.stashbot.DocumentService;
import com.embabel.stashbot.StashbotProperties;
import com.embabel.stashbot.user.StashbotUser;
import com.embabel.stashbot.user.StashbotUserService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Vaadin-based chat view for the RAG chatbot.
 */
@Route("")
@PageTitle("Stashbot")
@PermitAll
public class ChatView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(ChatView.class);

    private final Chatbot chatbot;
    private final String persona;
    private final StashbotProperties properties;
    private final DocumentService documentService;
    private final StashbotUser currentUser;

    private VerticalLayout messagesLayout;
    private Scroller messagesScroller;
    private TextField inputField;
    private Button sendButton;
    private Footer footer;
    private UserSection userSection;

    public ChatView(Chatbot chatbot, StashbotProperties properties, DocumentService documentService,
                    StashbotUserService userService) {
        this.chatbot = chatbot;
        this.properties = properties;
        this.documentService = documentService;
        this.currentUser = userService.getAuthenticatedUser();
        this.persona = properties.voice().persona();

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header row with title and user section
        var headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(Alignment.CENTER);
        headerRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerRow.setPadding(false);

        // Title section (left)
        var titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection.setSpacing(false);

        var title = new H3("Stashbot");
        title.addClassName("chat-title");

        var subtitle = new Span("RAG-powered document assistant");
        subtitle.addClassName("chat-subtitle");

        titleSection.add(title, subtitle);

        // User section (right)
        userSection = new UserSection(currentUser, documentService);
        headerRow.add(titleSection, userSection);
        add(headerRow);

        // Messages container
        messagesLayout = new VerticalLayout();
        messagesLayout.setWidthFull();
        messagesLayout.setPadding(false);
        messagesLayout.setSpacing(true);

        messagesScroller = new Scroller(messagesLayout);
        messagesScroller.setSizeFull();
        messagesScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        messagesScroller.addClassName("chat-scroller");
        add(messagesScroller);
        setFlexGrow(1, messagesScroller);

        // Restore previous messages if session exists
        restorePreviousMessages();

        // Input section
        add(createInputSection());

        // Footer
        footer = new Footer(documentService.getDocumentCount(), documentService.getChunkCount());
        add(footer);

        // Documents drawer
        var drawer = new DocumentsDrawer(documentService, currentUser, this::refreshFooter);
        getElement().appendChild(drawer.getElement());
    }

    private void refreshFooter() {
        remove(footer);
        footer = new Footer(documentService.getDocumentCount(), documentService.getChunkCount());
        add(footer);
        userSection.refreshContexts();
    }

    private record SessionData(ChatSession chatSession, BlockingQueue<Message> responseQueue) {
    }

    private SessionData getOrCreateSession(UI ui) {
        var vaadinSession = VaadinSession.getCurrent();
        var sessionData = (SessionData) vaadinSession.getAttribute("sessionData");

        if (sessionData == null) {
            var responseQueue = new ArrayBlockingQueue<Message>(10);
            var outputChannel = new VaadinOutputChannel(responseQueue, ui);
            var chatSession = chatbot.createSession(currentUser, outputChannel, UUID.randomUUID().toString());
            sessionData = new SessionData(chatSession, responseQueue);
            vaadinSession.setAttribute("sessionData", sessionData);
            logger.info("Created new chat session");
        }

        return sessionData;
    }

    private HorizontalLayout createInputSection() {
        var inputSection = new HorizontalLayout();
        inputSection.setWidthFull();
        inputSection.setPadding(false);
        inputSection.setAlignItems(Alignment.CENTER);

        inputField = new TextField();
        inputField.setPlaceholder("Type your message...");
        inputField.setWidthFull();
        inputField.setClearButtonVisible(true);
        inputField.addKeyPressListener(Key.ENTER, e -> sendMessage());

        sendButton = new Button("Send", VaadinIcon.PAPERPLANE.create());
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> sendMessage());

        inputSection.add(inputField, sendButton);
        inputSection.setFlexGrow(1, inputField);

        return inputSection;
    }

    private void sendMessage() {
        var text = inputField.getValue();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        inputField.clear();
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        // Add user message to UI
        messagesLayout.add(ChatMessageBubble.user(text));
        scrollToBottom();

        var ui = getUI().orElse(null);
        if (ui == null) return;

        var sessionData = getOrCreateSession(ui);

        new Thread(() -> {
            try {
                var userMessage = new UserMessage(text);
                logger.info("Sending user message: {}", text);
                sessionData.chatSession().onUserMessage(userMessage);

                var response = sessionData.responseQueue().poll(60, TimeUnit.SECONDS);

                ui.access(() -> {
                    if (response != null) {
                        var content = response.getContent();
                        messagesLayout.add(ChatMessageBubble.assistant(persona, content));
                    } else {
                        messagesLayout.add(ChatMessageBubble.error("Response timed out"));
                    }
                    scrollToBottom();
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.focus();
                });
            } catch (Exception e) {
                logger.error("Error getting chatbot response", e);
                ui.access(() -> {
                    messagesLayout.add(ChatMessageBubble.error("Error: " + e.getMessage()));
                    scrollToBottom();
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void scrollToBottom() {
        messagesScroller.getElement().executeJs("this.scrollTop = this.scrollHeight");
    }

    private void restorePreviousMessages() {
        var vaadinSession = VaadinSession.getCurrent();
        var sessionData = (SessionData) vaadinSession.getAttribute("sessionData");
        if (sessionData == null) {
            return;
        }

        var conversation = sessionData.chatSession().getConversation();
        for (var message : conversation.getMessages()) {
            if (message instanceof UserMessage) {
                messagesLayout.add(ChatMessageBubble.user(message.getContent()));
            } else if (message instanceof AssistantMessage) {
                messagesLayout.add(ChatMessageBubble.assistant(persona, message.getContent()));
            }
        }

        if (!conversation.getMessages().isEmpty()) {
            scrollToBottom();
        }
    }

    /**
     * Output channel that queues messages and displays tool calls in real-time.
     */
    private class VaadinOutputChannel implements OutputChannel {
        private final BlockingQueue<Message> queue;
        private final UI ui;
        private Div currentToolCallIndicator;

        VaadinOutputChannel(BlockingQueue<Message> queue, UI ui) {
            this.queue = queue;
            this.ui = ui;
        }

        @Override
        public void send(OutputChannelEvent event) {
            if (event instanceof MessageOutputChannelEvent msgEvent) {
                var msg = msgEvent.getMessage();
                if (msg instanceof AssistantMessage) {
                    // Remove tool call indicator before showing response
                    ui.access(() -> {
                        if (currentToolCallIndicator != null) {
                            messagesLayout.remove(currentToolCallIndicator);
                            currentToolCallIndicator = null;
                        }
                    });
                    queue.offer(msg);
                }
            } else if (event instanceof ProgressOutputChannelEvent progressEvent) {
                ui.access(() -> {
                    // Remove previous indicator if exists
                    if (currentToolCallIndicator != null) {
                        messagesLayout.remove(currentToolCallIndicator);
                    }
                    // Create new tool call indicator
                    currentToolCallIndicator = new Div();
                    currentToolCallIndicator.addClassName("tool-call-indicator");
                    currentToolCallIndicator.setText(progressEvent.getMessage());
                    messagesLayout.add(currentToolCallIndicator);
                    scrollToBottom();
                });
            }
        }
    }
}
