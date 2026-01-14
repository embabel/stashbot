package com.embabel.stashbot.vaadin;

import com.embabel.stashbot.DocumentService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Document list section for the documents drawer.
 * Shows statistics about indexed documents and chunks.
 */
public class DocumentListSection extends VerticalLayout {

    private final DocumentService documentService;
    private final Span documentCountSpan;
    private final Span chunkCountSpan;

    public DocumentListSection(DocumentService documentService, Runnable onDocumentsChanged) {
        this.documentService = documentService;

        setPadding(true);
        setSpacing(true);

        var title = new H4("Index Statistics");
        title.addClassName("section-title");

        var statsContainer = new Div();
        statsContainer.addClassName("stats-container");

        documentCountSpan = new Span();
        documentCountSpan.addClassName("stat-value");

        chunkCountSpan = new Span();
        chunkCountSpan.addClassName("stat-value");

        var docRow = createStatRow("Documents", documentCountSpan);
        var chunkRow = createStatRow("Chunks", chunkCountSpan);

        statsContainer.add(docRow, chunkRow);

        add(title, statsContainer);

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
    }
}
