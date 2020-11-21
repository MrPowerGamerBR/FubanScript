package br.pucsp.fubanscript.visitors

import FubanScriptBaseVisitor
import br.pucsp.fubanscript.utils.Constants
import br.pucsp.fubanscript.utils.ValueReturnedException
import br.pucsp.fubanscript.visitors.values.*
import java.lang.RuntimeException

class FubanScriptVisitor(
    val variables: MutableMap<String, ScriptValue>,
    val functions: MutableMap<String, FubanScriptParser.FunctionDeclarationContext>
) : FubanScriptBaseVisitor<ScriptValue>() {
    override fun visitFunctionDeclaration(ctx: FubanScriptParser.FunctionDeclarationContext): ScriptValue {
        val funId = ctx.ID().text
        // println("Fun Declaration $funId")
        functions[funId] = ctx
        return ScriptValue.VOID
    }

    override fun visitSetVariable(ctx: FubanScriptParser.SetVariableContext): ScriptValue {
        // println(ctx.text)

        val typeName = ctx.type()?.text
        val variableId = ctx.ID()?.text
        // println(ctx.type()?.text)
        // println(ctx.ID())
        // println(ctx.expression()?.text)

        val expr = visit(ctx.expression())
        // println("Expression:")
        // println(expr)

        variables[variableId!!] = expr

        return ScriptValue.VOID
    }

    override fun visitRedefineVariable(ctx: FubanScriptParser.RedefineVariableContext): ScriptValue {
        // println(ctx.text)

        val currentVariable = variables[ctx.ID().text] ?: throw RuntimeException("Can't redefine a non declared variable!")

        val expr = visit(ctx.expression())

        if (expr is NumericValue && currentVariable !is NumericValue)
            throw RuntimeException("Can't redeclare with a different type")

        if (expr is NullableStringValue && currentVariable !is NullableStringValue)
            throw RuntimeException("Can't redeclare with a different type")

        if (expr is NullableStringValue && currentVariable is StringValue)
            throw RuntimeException("Transforming a non-null variable to null is disallowed!")

        variables[ctx.ID().text] = expr

        return ScriptValue.VOID
    }

    override fun visitIdAtom(ctx: FubanScriptParser.IdAtomContext): ScriptValue {
        return variables[ctx.ID().text] ?: ScriptValue.NULL
    }

    override fun visitIntegerAtom(ctx: FubanScriptParser.IntegerAtomContext): ScriptValue {
        return NumericValue(ctx.INT().text.toInt().toDouble())
    }

    override fun visitDoubleAtom(ctx: FubanScriptParser.DoubleAtomContext): ScriptValue {
        return NumericValue(ctx.DOUBLE().text.toInt().toDouble())
    }

    override fun visitBooleanAtom(ctx: FubanScriptParser.BooleanAtomContext): ScriptValue {
        return when (ctx.text) {
            "true" -> BooleanValue(true)
            "false" -> BooleanValue(false)
            else -> throw java.lang.IllegalArgumentException("Unsupported Boolean Atom: ${ctx.text}")
        }
    }

    override fun visitStringAtom(ctx: FubanScriptParser.StringAtomContext): ScriptValue {
        return StringValue(
            ctx.STRING().text
                .removePrefix("\"")
                .removeSuffix("\"")
        )
    }

    override fun visitEqualityExpr(ctx: FubanScriptParser.EqualityExprContext): ScriptValue {
        val left = visit(ctx.expression(0))
        val right = visit(ctx.expression(1))

        // println("equality $left $right")
        // println(ctx.op)

        val result = left == right

        // println("Equality Result: $result")

        return BooleanValue(
            when (ctx.op.type) {
                FubanScriptParser.EQ -> result
                FubanScriptParser.NEQ -> !result
                else -> throw RuntimeException("Unknown operator ${ctx.op} ${ctx.op.type}")
            }
        )
    }

    override fun visitAdditiveExpr(ctx: FubanScriptParser.AdditiveExprContext): ScriptValue {
        // println("Variables: " + variables)
        val left = visit(ctx.expression(0)) as NumericValue
        val right = visit(ctx.expression(1)) as NumericValue

        val leftInt = left.value
        val rightInt = right.value
        // println("comparsion $left $right")
        // println(ctx.op)

        return NumericValue(
            when (ctx.op.type) {
                FubanScriptParser.PLUS -> leftInt + rightInt
                FubanScriptParser.MINUS -> leftInt - rightInt
                FubanScriptParser.MULTIPLICATION -> leftInt * rightInt
                FubanScriptParser.DIVISION -> leftInt / rightInt
                FubanScriptParser.MOD -> leftInt % rightInt
                FubanScriptParser.EXPONENTIAL -> Math.pow(leftInt, rightInt)
                else -> throw RuntimeException("Unknown operator ${ctx.op}")
            }
        )
    }

    override fun visitRelationalExpr(ctx: FubanScriptParser.RelationalExprContext): ScriptValue {
        val left = visit(ctx.expression(0)) as NumericValue
        val right = visit(ctx.expression(1)) as NumericValue

        // println("comparsion $left $right")
        // println(ctx.op)

        return BooleanValue(
            when (ctx.op.type) {
                FubanScriptParser.GT -> (left.value) > (right.value)
                FubanScriptParser.GTEQ -> (left.value) >= (right.value)
                FubanScriptParser.LT -> (left.value) < (right.value)
                FubanScriptParser.LTEQ -> (left.value) <= (right.value)
                else -> throw RuntimeException("Unknown operator ${ctx.op}")
            }
        )
    }

    override fun visitIfStatement(ctx: FubanScriptParser.IfStatementContext): ScriptValue {
        val result = visit(ctx.expression()) as BooleanValue

        if (result.value) {
            // println("VISIT #1")
            visit(ctx.codebody(0))
        } else if (ctx.ifStatement() != null) {
            // println("VISIT #2")
            visit(ctx.ifStatement())
        } else if (ctx.codebody(1) != null) {
            // println("VISIT #3")
            visit(ctx.codebody(1))
        }

        return ScriptValue.VOID
    }

    override fun visitWhileStatement(ctx: FubanScriptParser.WhileStatementContext): ScriptValue {
        while ((visit(ctx.expression()) as BooleanValue).value) {
            visit(ctx.codebody())
        }

        return ScriptValue.VOID
    }

    override fun visitNullAtom(ctx: FubanScriptParser.NullAtomContext) = ScriptValue.NULL

    override fun visitElvisExpr(ctx: FubanScriptParser.ElvisExprContext): ScriptValue {
        val left = visit(ctx.expression(0))
        val right = visit(ctx.expression(1))

        if (left == ScriptValue.NULL) return right
        return left
    }

    override fun visitInstruction(ctx: FubanScriptParser.InstructionContext): ScriptValue {
        val id = ctx.ID().text

        val arguments =
            ctx.arguments().expression().map { visit(it) }

        when (id) {
            "println" -> {
                println(arguments.joinToString(" "))
            }
            "print" -> {
                print(arguments.joinToString(" "))
            }
            else -> {
                val function = functions[id] ?: throw IllegalArgumentException("Unknown Instruction: $id")

                val argumentIds = function.functionArguments().ID()
                    .map {
                        it.text
                    }

                if (argumentIds.size != arguments.size)
                    throw IllegalArgumentException("Incompatible number of arguments for function $id! Arguments: $arguments; Function Arguments: ${argumentIds}")

                val variablesForFunction = variables.toMutableMap()
                for ((index, argumentId) in argumentIds.withIndex()) {
                    variablesForFunction[argumentId] = arguments[index]
                }

                // When returning a value, the function writes a variable called "return0"
                //
                // If the variable is not present, then it means that the script returns VOID!
                try {
                    FubanScriptVisitor(
                        variablesForFunction,
                        functions
                    ).visit(function.codebody())
                } catch (e: ValueReturnedException) {}

                // println("Local Scope Variables: $variablesForFunction")

                // println("We are going to return ${variablesForFunction[Constants.RETURN_VARIABLE_NAME]}")

                return variablesForFunction[Constants.RETURN_VARIABLE_NAME] ?: ScriptValue.VOID
            }
        }

        return ScriptValue.VOID
    }

    override fun visitReturnStatement(ctx: FubanScriptParser.ReturnStatementContext): ScriptValue {
        // println("RETURN STATEMENT")
        val value = visit(ctx.expression())

        // println("Writing ${Constants.RETURN_VARIABLE_NAME} to $value")
        variables[Constants.RETURN_VARIABLE_NAME] = value

        // println("Return Statement Variables: $variables")

        throw ValueReturnedException()
    }
}