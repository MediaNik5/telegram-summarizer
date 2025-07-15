# AI-Powered Personal Notes Telegram Bot

This project implements a Telegram bot that stores your notes and answers
questions about them using a Retrieval-Augmented Generation workflow.

## Building

```
./gradlew build
```

## Running

Set the following environment variables before starting the bot:

- `TELEGRAM_BOT_TOKEN` – your Telegram bot token
- `OPENAI_API_KEY` – OpenAI API key
- `CHROMA_URL` – URL of a running ChromaDB instance (for example
  `http://localhost:8000` when using the persistent client)

Optionally you can set `TELEGRAM_BOT_USERNAME` to override the displayed name.

Start the bot with:

```
./gradlew run
```
