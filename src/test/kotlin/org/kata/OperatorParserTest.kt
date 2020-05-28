package org.kata

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum

class OperatorParserTest : StringSpec({
    "should parse operator" {
        checkAll(Exhaustive.Companion.enum<Operator>()) { operator ->
            val result = ComputationOperatorParser.parse(operator.symbol.toString())
            result shouldBe ComputationParseResult(listOf(ComputationOperator(operator)), "")
        }
    }
    "should parse only the operator part at the beginning of the line" {
        val result = ComputationOperatorParser.parse("+++22")
        result shouldBe ComputationParseResult(listOf(ComputationOperator(Operator.PLUS)), "22")
    }
    "should handle special rule with - symbol" {
        val result = ComputationOperatorParser.parse("--22")
        result shouldBe ComputationParseResult(listOf(ComputationOperator(Operator.PLUS)), "22")
    }
    "should return null if line doesn't start with operator" {
        val result = ComputationOperatorParser.parse("22")
        result shouldBe null
    }
})
