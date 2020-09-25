package bot

import bindings.RustDefs
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.cdimascio.dotenv.dotenv

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RustDefs.exampleMethod()
        }
    }
}