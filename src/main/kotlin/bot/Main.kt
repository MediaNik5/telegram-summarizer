package bot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.SendMessage

fun main() {
    val token = System.getenv("TELEGRAM_BOT_TOKEN") ?: return
    val bot = TelegramBot(token)

    bot.setUpdatesListener({ updates ->
        updates.forEach { update ->
            val chatId = update.message()?.chat()?.id()
            if (chatId != null) {
                bot.execute(SendMessage(chatId, "Hello from bot"))
            }
        }
        UpdatesListener.CONFIRMED_UPDATES_ALL
    })
}
