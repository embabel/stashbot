# Stashbot

RAG-powered document chatbot with Vaadin UI.

## Features

- Lucene-based vector search for documents
- Vaadin web interface
- Configurable personas and objectives
- Markdown rendering in chat responses

## Running

```bash
mvn spring-boot:run
```

Then open http://localhost:8888 in your browser.

## Configuration

Edit `src/main/resources/application.yml` to configure:
- LLM model and temperature
- Chunking parameters
- Persona and objective

## Adding Documents

Place documents in the `data/` directory and they will be indexed on startup.
