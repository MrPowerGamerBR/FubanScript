package br.pucsp.fubanscript

import FubanScriptLexer
import FubanScriptParser
import br.pucsp.fubanscript.visitors.FubanScriptVisitor
import br.pucsp.fubanscript.visitors.values.NumericValue
import br.pucsp.fubanscript.visitors.values.ScriptValue
import br.pucsp.fubanscript.visitors.values.StringValue
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import kotlin.system.exitProcess

object FubanScriptLauncher  {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("FUBANSCRIPT (Protótipo)")
            println("fubanscript.jar nome_do_arquivo.ls")
            exitProcess(0)
        }

        val fileName = args[0]

        val fileInput = File(fileName)

        if (!fileInput.exists()) {
            println("Arquivo $fileName não existe!")
            exitProcess(1)
        }

        val scriptArguments = args.drop(1)
            .map {
                // Parse (optional) script arguments
                val doubleValue = it.toDoubleOrNull()

                if (doubleValue != null) {
                    NumericValue(doubleValue)
                } else {
                    StringValue(it)
                }
            }

        val input = CharStreams.fromString(fileInput.readText())

        val lexer = FubanScriptLexer(input)
        val parser = FubanScriptParser(CommonTokenStream(lexer))

        val visitor = FubanScriptVisitor(
            mutableMapOf<String, ScriptValue>().apply {
                for ((index, value) in scriptArguments.withIndex()) {
                    this["arg$index"] = value
                }
            },
            mutableMapOf()
        )

        visitor.visit(parser.body())
        // Start parsing
        // parser.instruction().enterRule(listener)

        println("VM Variables: ${visitor.variables}")
    }
}