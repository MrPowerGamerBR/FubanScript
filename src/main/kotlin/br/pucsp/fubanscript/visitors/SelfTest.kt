package br.pucsp.fubanscript.visitors

import FubanScriptLexer
import FubanScriptParser
import br.pucsp.fubanscript.visitors.values.ScriptValue
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main() {
    val input = CharStreams.fromString("""
Int a = 0

while (a != 3000) {
    Boolean isDivisibleBy4 = a % 4 == 0;

    if (isDivisibleBy4) {
        Boolean isDivisibleBy100 = a % 100 == 0;

        if (isDivisibleBy100) {
            print("Ano ")
            print(a)
            print(" é bissexto!")
            println("")
        } else {
            Boolean isDivisibleBy400 = a % 400 == 0;

            if (isDivisibleBy400) {
                // Ano comum
            } else {
                print("Ano ")
                print(a)
                print(" é bissexto!")
                println("")
            }
        }
    } else {
        // Ano comum
    }

    a = a + 1
}
    """.trimIndent())

    val lexer = FubanScriptLexer(input)
    val parser = FubanScriptParser(CommonTokenStream(lexer))

    val visitor = FubanScriptVisitor(
        mutableMapOf<String, ScriptValue>(),
        mutableMapOf()
    )

    visitor.visit(parser.body())
    // Start parsing
    // parser.instruction().enterRule(listener)

    println("VM Variables: ${visitor.variables}")
}