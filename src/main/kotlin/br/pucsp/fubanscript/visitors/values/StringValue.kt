package br.pucsp.fubanscript.visitors.values

class StringValue(override val value: String) : NullableStringValue(value) {
    override fun toString() = value
}