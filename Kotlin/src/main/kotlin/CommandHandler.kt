package bot

public class CommandHandler {
    enum class ArgType {
        NUMBER,
        STRING
    }

    class Command(val commandName: String, private val numArgs: Int, argTypes: Array<ArgType>, private val callback: (args: List<String>) -> Unit) {

        public fun execute(args: List<String>): Boolean {
            if (args.size == this.numArgs) {
                this.callback(args)
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

    public fun addCommand(commandName: String, numArgs: Int, argTypes: Array<ArgType>) {
        commands.add(Command(commandName, numArgs, argTypes, {

        }))
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