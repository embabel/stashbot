package com.embabel.stashbot.vaadin;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * Footer component showing copyright and document statistics.
 */
public class Footer extends HorizontalLayout {

    public Footer(int documentCount, int chunkCount) {
        setWidthFull();
        setPadding(false);
        setSpacing(true);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("app-footer");

        var copyright = new Span("© Embabel 2026");
        copyright.addClassName("footer-copyright");

        var separator = new Span("·");
        separator.addClassName("footer-separator");

        var stats = new Span(documentCount + " documents · " + chunkCount + " chunks");
        stats.addClassName("footer-stats");

        add(copyright, separator, stats);
    }
}
