# Telegram Notes Bot

This project contains a simple Java Telegram bot that stores your notes and lets you ask questions about them using OpenAI and ChromaDB.

## Requirements
- Java 21
- A running [Chroma](https://docs.trychroma.com/) server. You can start one locally with persistence:
  ```bash
  pip install chromadb
  python -m chromadb --path ./chroma
  ```
- Environment variables:
  - `TELEGRAM_BOT_TOKEN` – token of your Telegram bot
  - `TELEGRAM_BOT_USERNAME` – bot username (optional)
  - `OPENAI_API_KEY` – API key for OpenAI
  - `CHROMA_URL` – URL of the running Chroma server (default `http://localhost:8000`)

## Building
```bash
./gradlew build
```

## Running
```bash
./gradlew run
```

## Usage
- Send any regular message to save it as a note.
- Ask questions with the `/ask` command:
  ```
  /ask What restaurants do I prefer?
  ```
