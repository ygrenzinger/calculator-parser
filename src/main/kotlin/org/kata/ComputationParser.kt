package org.kata

import org.kata.ComputationElementParser.Companion.optional
import org.kata.ComputationElementParser.Companion.repeat

data class ComputationParseResult(val result: List<ParserElement>, val remaining: String) {
    operator fun plus(other: ComputationParseResult) = ComputationParseResult(result + other.result, other.remaining)
}

sealed class ComputationElementParser {
    abstract fun parse(line: String): ComputationParseResult?
    fun and(other: ComputationElementParser) = AndCombinator(this, other)
    fun or(other: ComputationElementParser) = OrCombinator(this, other)

    companion object {
        fun repeat(parser: ComputationElementParser) = RepeatCombinator(parser)
        fun optional(parser: ComputationElementParser) = OptionalParser(parser)
    }
}

data class AndCombinator(val a: ComputationElementParser, val b: ComputationElementParser) :
    ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        return a.parse(line)?.let { aResult ->
            return b.parse(aResult.remaining)?.let {
                aResult + it
            }
        }
    }
}

data class OrCombinator(val a: ComputationElementParser, val b: ComputationElementParser) :
    ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        return a.parse(line) ?: b.parse(line)
    }
}

data class OptionalParser(val parser: ComputationElementParser) : ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        return parser.parse(line) ?: ComputationParseResult(listOf(), line)
    }
}

data class RepeatCombinator(val parser: ComputationElementParser) : ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        return parser.parse(line)?.let {
            iterateParser(it)
        }
    }

    private fun iterateParser(computationParseResult: ComputationParseResult): ComputationParseResult {
        return if (computationParseResult.remaining.isEmpty()) {
            computationParseResult
        } else {
            parser.parse(computationParseResult.remaining)?.let {
                iterateParser(ComputationParseResult(computationParseResult.result + it.result, it.remaining))
            } ?: computationParseResult
        }
    }
}

object ComputationNumberParser : ComputationElementParser() {
    private val numberRegex = """^(-?\d+).*""".toRegex()
    override fun parse(line: String): ComputationParseResult? {
        return numberRegex.find(line)?.let {
            val (number) = it.destructured
            ComputationParseResult(listOf(ComputationNumber(number.toInt())), line.drop(number.length))
        }
    }
}

object ComputationVariableParser : ComputationElementParser() {
    private val variableRegex = """^([a-zA-Z]+).*""".toRegex()
    override fun parse(line: String): ComputationParseResult? {
        return variableRegex.find(line)?.let {
            val (name) = it.destructured
            ComputationParseResult(listOf(ComputationVariable(name)), line.drop(name.length))
        }
    }

}

object ComputationOperatorParser : ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        val op = Operator.allOperatorSymbols[line.first()]
        return op?.let {
            val remaining = line.dropWhile { it == op.symbol }
            val number = line.length - remaining.length
            return if (op == Operator.MINUS && number % 2 == 0) {
                ComputationParseResult(listOf(ComputationOperator(Operator.PLUS)), remaining)
            } else if (op == Operator.PLUS || op == Operator.MINUS ) {
                ComputationParseResult(listOf(ComputationOperator(it)), remaining)
            } else if (number == 1) {
                ComputationParseResult(listOf(ComputationOperator(it)), remaining)
            } else {
                null
            }
        }
    }
}

open class ParenthesisParser(private val parenthesis: Parenthesis) : ComputationElementParser() {
    override fun parse(line: String): ComputationParseResult? {
        if (line.isNotEmpty() && line.first() == parenthesis.symbol) {
            return ComputationParseResult(listOf(parenthesis), line.drop(1))
        }
        return null
    }
}

object LeftParenthesisParser : ParenthesisParser(LeftParenthesis)
object RightParenthesisParser : ParenthesisParser(RightParenthesis)

object ComputationParser {
    private val numberOrVariable = ComputationNumberParser.or(ComputationVariableParser)
    private val optionalLeftParenthesis = optional(repeat(LeftParenthesisParser))
    private val optionalRightParenthesis = optional(repeat(RightParenthesisParser))
    private val repeativePart = ComputationOperatorParser
        .and(optionalLeftParenthesis)
        .and(numberOrVariable)
        .and(optionalRightParenthesis)
    val parser = optionalLeftParenthesis.and(numberOrVariable).and(repeat(repeativePart))

    fun parse(line: String): List<ParserElement> {
        val parsed = parser.parse(line.replace(" ", ""))
        return parsed?.result ?: throw ParseException("Invalid expression")
    }
}
