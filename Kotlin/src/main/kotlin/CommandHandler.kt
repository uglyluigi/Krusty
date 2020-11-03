package bot

import bot.CommandHandler.Command.Companion.ExecutionResult.*
import discord4j.core.`object`.entity.Message

class CommandHandler {
    private val commands: MutableList<Command> = mutableListOf()

    enum class ArgType {
        NUMBER,
        STRING
    }

    class Command(
        val commandName: String,
        private val argRange: ArgRange,
        private val numArgs: Int,
        private val argNames: Array<String>,
        private val argTypes: Array<ArgType>,
        private val callback: (args: List<Any>, message: Message) -> Unit
    ) {
        val helpString: String = buildHelpString()

        companion object {
            enum class ExecutionResult {
                TOO_MANY_ARGS,
                TOO_FEW_ARGS,
                WRONG_ARG_TYPE,
                SUCCESS,
                WTF
            }

            enum class ArgRange {
                EXACTLY,
                AT_LEAST,
                AT_MOST,
                DONT_CARE_DIDNT_ASK
            }
        }

        fun execute(args: List<String>, message: Message): ExecutionResult {
            if (
                when (this.argRange) {
                    ArgRange.EXACTLY -> args.size == this.numArgs
                    ArgRange.AT_LEAST -> args.size >= this.numArgs
                    ArgRange.AT_MOST -> args.size <= this.numArgs
                    ArgRange.DONT_CARE_DIDNT_ASK -> true
                }
            ) {
                if (args.size != this.argTypes.size) {
                    throw IllegalArgumentException("Arg list size(=${args.size}) doesn\'t match arg type list size(=${this.argTypes.size}")
                }

                val argList = mutableListOf<Any>()

                for ((i, argType) in this.argTypes.withIndex()) {
                    val token = args[i]
                    argList.add(
                        when (argType) {
                            ArgType.NUMBER -> {
                                token.toIntOrNull()?.let {
                                    return@let it
                                }

                                return WRONG_ARG_TYPE
                            }

                            ArgType.STRING -> token
                        }
                    )
                }

                this.callback(argList, message)
                return SUCCESS
            } else {
                return when (this.argRange) {
                    ArgRange.EXACTLY -> when {
                        args.size < this.numArgs -> TOO_FEW_ARGS
                        args.size > this.numArgs -> TOO_MANY_ARGS
                        else -> WTF
                    }
                    ArgRange.AT_LEAST -> TOO_FEW_ARGS
                    ArgRange.AT_MOST -> TOO_MANY_ARGS
                    ArgRange.DONT_CARE_DIDNT_ASK -> WTF
                }
            }
        }


        private fun buildHelpString(): String {
            val sb: StringBuilder = StringBuilder()
            sb.append("!${this.commandName} ")

            for ((i, argName) in argNames.withIndex()) {
                val correspondingArgType = when (argTypes[i]) {
                    ArgType.NUMBER -> "Int"
                    ArgType.STRING -> "String"
                }

                sb.append("<${argName} [$correspondingArgType]> ")
            }

            return sb.toString()
        }
    }

    fun runCommand(token_str: String, msg: Message): Boolean {
        val tokens: List<String> = token_str.substring(1).split(" ")

        searchForCommand(tokens[0])?.let { cmd ->

            println("Found command " + tokens[0])

            val result = when (cmd.execute(tokens.slice(IntRange(1, tokens.size - 1)), msg)) {
                SUCCESS -> {
                    true
                }

                TOO_MANY_ARGS -> {
                    Helper.reply(msg, "You supplied too many arguments.")
                    false
                }

                TOO_FEW_ARGS -> {
                    Helper.reply(msg, "You didn\'t supply enough arguments.")
                    false
                }

                WRONG_ARG_TYPE -> {
                    Helper.reply(msg, "You didn\'t supply the right type of arguments.")
                    false
                }

                WTF -> {
                    Helper.reply(msg, "Uhoh, I got returned WTF. That can\'t be good")
                    false
                }
            }

            if (!result) {
                Helper.sayUsage(msg, cmd)
            }

            return result
        }

        return false
    }


    fun addCommand(
        commandName: String,
        argRange: Command.Companion.ArgRange,
        numArgs: Int,
        argNames: Array<String>,
        argTypes: Array<ArgType>,
        callback: (args: List<Any>, message: Message) -> Unit
    ) {
        commands.add(Command(commandName, argRange, numArgs, argNames, argTypes, callback))
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