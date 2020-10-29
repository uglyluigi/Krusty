package bot

import discord4j.core.`object`.entity.Message
import java.io.File

class Helper {
    companion object {
        fun reply(msg: Message, s: String) {
            msg.channel.block()?.createMessage(s)?.block()
        }

        fun sayUsage(msg: Message, cmd: CommandHandler.Command) {
            reply(msg, "Usage: ${cmd.helpString}")
        }

        fun emitImageResult(msg: Message, path: String) {
            val file = File(path)
            msg.channel.block()?.createMessage { spec ->
                spec.addFile("output.png", file.inputStream())
            }?.block()
        }
    }
}