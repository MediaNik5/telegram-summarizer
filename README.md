# Telegram Notes Bot

An experimental Java Telegram bot that stores your personal notes and allows asking natural language questions about them. Notes are embedded with OpenAI and stored in a PostgreSQL database with the pgvector extension enabled.

## Building

```
./gradlew build
```

## Running

The bot is configured entirely through environment variables:

- `TELEGRAM_BOT_TOKEN` – Telegram bot token
- `OPENAI_API_KEY` – API key for OpenAI
- `DATABASE_URL` – JDBC URL to a PostgreSQL instance with pgvector enabled
- `DB_USER` and `DB_PASSWORD` – database credentials

Start the bot with:

```
./gradlew run
```
