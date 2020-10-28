package bot

import discord4j.core.`object`.entity.Message

class Helper {
    companion object {
        fun reply(msg: Message, s: String) {
            msg.channel.block()?.createMessage(s)?.block()
        }

        fun sayUsage(msg: Message, cmd: CommandHandler.Command) {
            reply(msg, "Usage: ${cmd.helpString}")
        }
    }
}