package br.pucsp.fubanscript.visitors.values

open class NullableStringValue(open val value: String?) : ScriptValue() {
    override val type = ValueType.STRING
}