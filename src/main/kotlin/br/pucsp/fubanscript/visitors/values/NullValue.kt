package br.pucsp.fubanscript.visitors.values

class NullValue : ScriptValue() {
    override val type = ValueType.NULL

    override fun toString() = "null"
}