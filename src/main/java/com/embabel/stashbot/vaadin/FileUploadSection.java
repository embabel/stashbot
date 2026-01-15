package com.embabel.stashbot.vaadin;

import com.embabel.stashbot.DocumentService;
import com.embabel.stashbot.user.StashbotUser;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File upload section for the documents drawer.
 */
public class FileUploadSection extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadSection.class);

    public FileUploadSection(DocumentService documentService, StashbotUser user, Runnable onSuccess) {
        setPadding(true);
        setSpacing(true);

        var context = new DocumentService.Context(user);

        var instructions = new Span("Upload documents to add to the knowledge base");
        instructions.addClassName("section-instructions");

        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setAcceptedFileTypes(
                ".pdf", ".txt", ".md", ".html", ".htm",
                ".doc", ".docx", ".odt", ".rtf",
                "application/pdf",
                "text/plain",
                "text/markdown",
                "text/html",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB

        upload.addSucceededListener(event -> {
            var filename = event.getFileName();
            try {
                var inputStream = buffer.getInputStream();
                var uri = "upload://" + filename;
                documentService.ingestStream(inputStream, uri, filename, context);

                Notification.show("Uploaded: " + filename, 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                onSuccess.run();
            } catch (Exception e) {
                logger.error("Failed to ingest file: {}", filename, e);
                Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event -> {
            logger.error("Upload failed: {}", event.getReason().getMessage());
            Notification.show("Upload failed: " + event.getReason().getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        add(instructions, upload);
    }
}
