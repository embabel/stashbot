package com.embabel.stashbot.vaadin;

import com.embabel.stashbot.DocumentService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Document list section for the documents drawer.
 * Shows list of indexed documents with their context.
 */
public class DocumentListSection extends VerticalLayout {

    private final DocumentService documentService;
    private final Runnable onDocumentsChanged;
    private final VerticalLayout documentsList;
    private final Span documentCountSpan;
    private final Span chunkCountSpan;

    public DocumentListSection(DocumentService documentService, Runnable onDocumentsChanged) {
        this.documentService = documentService;
        this.onDocumentsChanged = onDocumentsChanged;

        setPadding(true);
        setSpacing(true);

        // Stats section
        var statsTitle = new H4("Statistics");
        statsTitle.addClassName("section-title");

        var statsContainer = new Div();
        statsContainer.addClassName("stats-container");

        documentCountSpan = new Span();
        documentCountSpan.addClassName("stat-value");

        chunkCountSpan = new Span();
        chunkCountSpan.addClassName("stat-value");

        statsContainer.add(createStatRow("Documents", documentCountSpan), createStatRow("Chunks", chunkCountSpan));

        // Documents list section
        var docsTitle = new H4("Documents");
        docsTitle.addClassName("section-title");
        docsTitle.getStyle().set("margin-top", "var(--lumo-space-m)");

        documentsList = new VerticalLayout();
        documentsList.setPadding(false);
        documentsList.setSpacing(false);
        documentsList.addClassName("documents-list");

        add(statsTitle, statsContainer, docsTitle, documentsList);

        refresh();
    }

    private Div createStatRow(String label, Span valueSpan) {
        var row = new Div();
        row.addClassName("stat-row");

        var labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");

        row.add(labelSpan, valueSpan);
        return row;
    }

    public void refresh() {
        documentCountSpan.setText(String.valueOf(documentService.getDocumentCount()));
        chunkCountSpan.setText(String.valueOf(documentService.getChunkCount()));

        documentsList.removeAll();

        var documents = documentService.getDocuments();
        if (documents.isEmpty()) {
            var emptyLabel = new Span("No documents indexed yet");
            emptyLabel.addClassName("empty-list-label");
            documentsList.add(emptyLabel);
        } else {
            for (var doc : documents) {
                documentsList.add(createDocumentRow(doc));
            }
        }
    }

    private HorizontalLayout createDocumentRow(DocumentService.DocumentInfo doc) {
        var row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.addClassName("document-row");

        var infoSection = new VerticalLayout();
        infoSection.setPadding(false);
        infoSection.setSpacing(false);

        var title = new Span(doc.title() != null ? doc.title() : doc.uri());
        title.addClassName("document-title");

        var contextBadge = new Span(doc.context());
        contextBadge.addClassName("context-badge");

        infoSection.add(title, contextBadge);

        var deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteButton.addClickListener(e -> {
            if (documentService.deleteDocument(doc.uri())) {
                Notification.show("Deleted: " + doc.title(), 3000, Notification.Position.BOTTOM_CENTER);
                refresh();
                onDocumentsChanged.run();
            } else {
                Notification.show("Failed to delete", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        row.add(infoSection, deleteButton);
        row.setFlexGrow(1, infoSection);

        return row;
    }
}
