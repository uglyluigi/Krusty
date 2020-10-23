package bot

import bindings.RustDefs
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.cdimascio.dotenv.dotenv
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

            val dotenv = dotenv()
            val client = DiscordClient.create(dotenv["BOT_TOKEN"])
            val gateway = client.login().block()

            var waitingForImageAction = false
            var waitingForBlendType = false

            gateway?.on(MessageCreateEvent::class.java)?.subscribe {
                    event: MessageCreateEvent ->
                val message = event.message
                val content = message.content

                if (waitingForImageAction) {
                    if (message.content == "blur") {
                        RustDefs.blurImage(Java2RustUtils.createInstance("image.png"))
                        println("Blurred your image.")
                        message.channel.block()?.createMessage("Blurred your image")
                        waitingForImageAction = false
                        return@subscribe
                    } else {
                        message.channel.block()?.createMessage("??")
                    }
                }


                if ("!ping" == content) {
                    val channel = message.channel.block()
                    channel?.createMessage("Pong!")?.block()
                } else if (message.attachments.isNotEmpty()) {
                    println("Image attachment detected")
                    val imageAttachment = message.attachments.first().url

                    try {
                        val url = URL(imageAttachment)
                        val image = ImageIO.read(url)
                        val outputFile = File("image.png")
                        ImageIO.write(image, "png", outputFile)
                        waitingForImageAction = true
                        message.channel.block()?.createMessage("What do you want me to do?")?.block()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                val handler: CommandHandler = CommandHandler()
                handler.addCommand("ping", 0, emptyArray()) { args, channel ->
                    channel.createMessage("Pong!")?.block()
                }
            }


            gateway?.onDisconnect()?.block()
        }
    }
}