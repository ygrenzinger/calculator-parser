package org.kata

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class NumberParserTest : StringSpec({
    "should parse integer" {
        checkAll<Int> { x ->
            val result = ComputationNumberParser.parse(x.toString())
            result shouldBe ComputationParseResult(listOf(ComputationNumber(x)), "")
        }
    }
    "should parse only the integer part at the beginning of the line" {
        val result = ComputationNumberParser.parse("-18+22")
        result shouldBe ComputationParseResult(listOf(ComputationNumber(-18)), "+22")
    }
    "should return null if line doesn't start with integer" {
        ComputationNumberParser.parse("-a+22") shouldBe null
    }
})
