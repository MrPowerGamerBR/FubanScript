package br.pucsp.fubanscript.visitors.values

class NumericValue(val value: Double) : ScriptValue() {
    override val type = ValueType.INTEGER

    override fun equals(other: Any?): Boolean {
        if (other is NumericValue)
            return other.value == value

        return false
    }

    override fun hashCode() = value.hashCode()
    override fun toString() = value.toString()
}