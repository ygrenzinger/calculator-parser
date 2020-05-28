package org.kata

sealed class ParserElement
sealed class ComputationElement : ParserElement()

data class ComputationNumber(val value: Int) : ComputationElement() {
    override fun toString() = value.toString()
}

data class ComputationVariable(val variable: String) : ComputationElement() {
    override fun toString() = variable
}

data class ComputationOperator(val operator: Operator) : ComputationElement() {
    fun combine(a: Int, b: Int) = operator.combine(a, b)
    override fun toString() = operator.symbol.toString()
}

sealed class Parenthesis(val symbol: Char) : ParserElement() {
    override fun toString() = symbol.toString()
}
object LeftParenthesis : Parenthesis('(')
object RightParenthesis : Parenthesis(')')
