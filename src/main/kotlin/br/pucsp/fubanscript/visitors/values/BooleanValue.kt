package br.pucsp.fubanscript.visitors.values

class BooleanValue(val value: Boolean) : ScriptValue() {
    override val type = ValueType.BOOLEAN

    override fun toString() = value.toString()
}