package org.kata

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.azstring

class VariableParserTest : StringSpec({
    "should parse variable" {
        checkAll(Exhaustive.Companion.azstring(1..2)) {
            val result = ComputationVariableParser.parse(it)
            result shouldBe ComputationParseResult(listOf(ComputationVariable(it)), "")
        }
    }
    "should parse only the variable part at the beginning of the line" {
        val result = ComputationVariableParser.parse("test+22")
        result shouldBe ComputationParseResult(listOf(ComputationVariable("test")), "+22")
    }
    "should return null if line doesn't start with variable" {
        val result = ComputationVariableParser.parse("22")
        result shouldBe null
    }
})
