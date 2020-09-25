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
            val dotenv = dotenv()
            val client = DiscordClient.create(dotenv["BOT_TOKEN"])
            val gateway = client.login().block()

            gateway?.on(MessageCreateEvent::class.java)?.subscribe {
                event: MessageCreateEvent ->
                val message = event.message

                if ("!ping" == event.message.content) {
                    val channel = message.channel.block()
                    channel?.createMessage("Pong!")?.block()
                }
            }

            gateway?.onDisconnect()?.block()
        }
    }
}