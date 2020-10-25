package bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel

public class CommandHandler {
    enum class ArgType {
        NUMBER,
        STRING
    }

    class Command(val commandName: String, private val numArgs: Int, argTypes: Array<ArgType>, private val callback: (args: List<String>, message: Message) -> Unit) {

        public fun execute(args: List<String>, message: Message): Boolean {
            if (args.size == this.numArgs) {
                this.callback(args, message)
            }

            return false
        }
    }

    public val commands: MutableList<Command> = mutableListOf()

    public fun runCommand(token_str: String, msg: Message): Boolean {
        val tokens: List<String> = token_str.substring(1).split(" ")

        searchForCommand(tokens[0])?.let {
            cmd ->
            println("Found command " + tokens[0])
            cmd.execute(tokens.slice(IntRange(1, tokens.size - 1)), msg)
            return@runCommand true
        }

        return false
    }

    public fun addCommand(commandName: String, numArgs: Int, argTypes: Array<ArgType>, callback: (args: List<String>, message: Message) -> Unit) {
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