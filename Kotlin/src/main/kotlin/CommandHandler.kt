package bot

import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.MessageChannel

public class CommandHandler {
    enum class ArgType {
        NUMBER,
        STRING
    }

    class Command(val commandName: String, private val numArgs: Int, argTypes: Array<ArgType>, private val callback: (args: List<String>, channel: MessageChannel) -> Unit) {

        public fun execute(args: List<String>, channel: MessageChannel): Boolean {
            if (args.size == this.numArgs) {
                this.callback(args, channel)
            }

            return false
        }
    }

    public val commands: MutableList<Command> = mutableListOf()

    public fun runCommand(token_str: String): Boolean {
        val tokens: List<String> = token_str.split(" ")

        searchForCommand(tokens[0])?.let {
            return@runCommand true
        }

        return false
    }

    public fun addCommand(commandName: String, numArgs: Int, argTypes: Array<ArgType>, callback: (args: List<String>, channel: MessageChannel) -> Unit) {
        commands.add(Command(commandName, numArgs, argTypes, callback))
    }

    fun searchForCommand(commandName: String): Command? {
        for (cmd: Command in commands) {
            if (cmd.commandName == commandName) {
                return cmd
            }
        }

        return null
    }
}