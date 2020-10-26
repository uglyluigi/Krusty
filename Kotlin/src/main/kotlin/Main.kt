package bot

import bindings.RustDefs
import bot.Hasher.Companion.md5
import bot.Hasher.Companion.sha256
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

            val f = File("poop.txt")
            val instance = Java2RustUtils.createInstance(f)


            val dotenv = dotenv()
            val client = DiscordClient.create(dotenv["BOT_TOKEN"])
            val gateway = client.login().block()

            var waitingForImageAction = false
            var waitingForBlendType = false

            val handler: CommandHandler = CommandHandler()

            handler.addCommand("ping", 0, emptyArray()) { args, msg ->
                print("Running !ping")
                msg.channel.block()?.createMessage("Pong!")?.block()
            }

            handler.addCommand("rotate", 1, arrayOf(CommandHandler.ArgType.NUMBER), {
                args, message ->

            })

            handler.addCommand("blur", 0, emptyArray()) { _, message ->
                if (message.attachments.isNotEmpty()) {
                    println("Downloading images")
                    val files = downloadImagesFrom(message)

                    for (f in files) {
                        println(f.absolutePath)
                    }
                }
            }

            gateway?.on(MessageCreateEvent::class.java)?.subscribe {
                    event: MessageCreateEvent ->
                val message = event.message
                val content = message.content

                if (content.isNotEmpty() && handler.runCommand(content, message)) {
                    println("Ran command: $content")
                }
            }

            gateway?.onDisconnect()?.block()
        }

        fun downloadImagesFrom(msg: Message): ArrayList<File> {
            val urls  = msg.attachments.map { URL(it.url) }
            val files = arrayListOf<File>()
            files.ensureCapacity(urls.size)
            val hash = urls.joinToString { it.path }.sha256()

            for ((index, url) in urls.withIndex()) {
                ImageIO.read(url)?.let {
                    val file = File("image_${hash}_$index.png")
                    ImageIO.write(it, "png", file)
                    files.add(file)
                }
            }

            return files
        }


    }
}