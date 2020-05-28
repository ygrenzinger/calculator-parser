package org.kata

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class CombineParserTest : StringSpec({
    "should combine parser with AND" {
        val parser = ComputationNumberParser
            .and(ComputationOperatorParser)
            .and(ComputationVariableParser)
        val expectedElements = listOf(
            ComputationNumber(-18),
            ComputationOperator(Operator.PLUS),
            ComputationVariable("number")
        )
        parser.parse("-18+number") shouldBe ComputationParseResult(expectedElements, "")
    }
    "should combine parser with OR" {
        val parser = ComputationNumberParser
            .and(ComputationOperatorParser)
            .and(ComputationNumberParser.or(ComputationVariableParser))
        val expectedElements = listOf(
            ComputationNumber(-18),
            ComputationOperator(Operator.PLUS)
        )
        parser.parse("-18+number") shouldBe ComputationParseResult(expectedElements + ComputationVariable("number"), "")
        parser.parse("-18+22") shouldBe ComputationParseResult(expectedElements + ComputationNumber(22), "")
    }
    "should combine and repeat parser" {
        val numberOrVariable = ComputationNumberParser.or(ComputationVariableParser)
        val parserToRepeat = ComputationOperatorParser.and(numberOrVariable)
        val parser = numberOrVariable.and(ComputationElementParser.repeat(parserToRepeat))

        val expectedElements = listOf(
            ComputationNumber(-18),
            ComputationOperator(Operator.PLUS),
            ComputationVariable("number"),
            ComputationOperator(Operator.MINUS),
            ComputationNumber(2),
            ComputationOperator(Operator.DIVIDE),
            ComputationVariable("nonZero")
        )

        parser.parse("-18--number---2/nonZero") shouldBe ComputationParseResult(expectedElements, "")
    }
    "should parse typical computation" {
        val result = ComputationParser.parser.parse("10/(3++(4+1)---2)*2")
        result.shouldNotBeNull()
        result.remaining shouldBe ""
        result.result.joinToString("") { it.toString() } shouldBe "10/(3+(4+1)-2)*2"
    }
})
