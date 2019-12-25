package com.github.happylynx.prick.cli

import com.github.happylynx.prick.cli.picocli.Main
import java.util.Arrays

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // TODO remove
        println("args " + Arrays.toString(args))

        val exitCode = Main.run(args)
        System.exit(exitCode)
    }
}
