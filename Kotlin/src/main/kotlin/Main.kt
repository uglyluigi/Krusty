package bot

import bindings.RustDefs

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            RustDefs.exampleMethod()
        }
    }
}