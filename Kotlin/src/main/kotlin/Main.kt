package bot

import bindings.RustDefs
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.cdimascio.dotenv.dotenv
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            if (false) {
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
}