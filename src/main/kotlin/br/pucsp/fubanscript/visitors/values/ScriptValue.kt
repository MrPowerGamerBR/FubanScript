package br.pucsp.fubanscript.visitors.values

import java.lang.RuntimeException

abstract class ScriptValue {
    companion object {
        val VOID = VoidValue()

        val NULL = NullValue()

        fun of(value: ScriptValue, type: FubanScriptParser.TypeContext?): ScriptValue {
            var isNullable = false
            var valueType: ValueType? = null

            println(value)

            if (type != null) {
                isNullable = type.MAYBE_NULL() != null

                valueType = when {
                    type.STR_TYPE() != null -> ValueType.STRING
                    type.INT_TYPE() != null -> ValueType.INTEGER
                    else -> null
                }
            }

            if (valueType == null) {
                valueType = when (value) {
                    is NullableStringValue -> ValueType.STRING
                    is NumericValue -> ValueType.INTEGER
                    else -> throw RuntimeException("Unsupported value type!")
                }
            }

            println("Creating Value Type $valueType, is Nullable? $isNullable")

            return when (valueType) {
                ValueType.NULL -> NULL
                ValueType.VOID -> VOID
                ValueType.STRING -> {
                    if (isNullable)
                        NullableStringValue(value as String?)
                    else
                        StringValue(value as String)
                }
                ValueType.INTEGER -> {
                    value as NumericValue
                }
                ValueType.BOOLEAN -> {
                    BooleanValue(value as Boolean)
                }
                else -> throw RuntimeException("oof")
            }
        }
    }

    abstract val type: ValueType

    val isNullable: Boolean = this::class.simpleName!!.startsWith("Null")

    /* open val functions = mutableMapOf<String, KFunction<*>>(
        "helloWorld" to ScriptValue::helloWorld,
        "helloWorldWithArgument" to ScriptValue::helloWorldWithArgument
    )

    fun helloWorld() {
        println("HELLO WORLD!")
    }

    fun helloWorldWithArgument(str: String) {
        println("HELLO WORLD! $str")
    }

    override fun toString(): String {
        return "vm_value:${value.toString()}"
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        println("equality check")
        if (value === other)
            return true

        /* if (value == null || other == null || other.javaClass != value.javaClass) {
            return false
        } */

        val that: ScriptValue = other as ScriptValue
        return value == that.value
    } */
}