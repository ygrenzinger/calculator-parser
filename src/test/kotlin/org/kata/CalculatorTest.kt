package org.kata

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.kata.ComputationParser.parse
import org.kata.InfixToPostfixConverter.convert

class CalculatorTest : StringSpec({
    "should convert infix to postfix" {
        forAll(
            row("3 - 2 + 4", "3 2 - 4 +"),
            row("3 + 2 * 4", "3 2 4 * +"),
            row("(3 + 2) * 4", "3 2 + 4 *"),
            row("2 * (3 + 4) + 1", "2 3 4 + * 1 +"),
            row("2 * (3 + 4) + 1", "2 3 4 + * 1 +"),
            row("8 * 3 + 12 * (4 - 2)", "8 3 * 12 4 2 - * +"),
            row("x + y / (5 * z) + 10", "x y 5 z * / + 10 +")
        ) { input, expected ->
            val postfix = convert(parse(input))
            postfix.joinToString(" ") { it.toString() } shouldBe expected
        }
    }
    "should throw exception if computation is invalid" {
        forAll(
            row("((5 * z) + 10"),
            row("(5 * z) + 10)"),
            row("(5 * z() + 10)"),
            row("()5 * z) + 10")
        ) { input ->
            val exception = shouldThrow<ParseException> {
                convert(parse(input))
            }
            exception.message shouldBe "Invalid expression"
        }
    }
    "should compute expression" {
        val calculator = Calculator()
        calculator.evaluateAssignment("a=4")
        calculator.evaluateAssignment("b=5")
        calculator.evaluateAssignment("c=6")
        forAll(
            row("2 - 2 + 3", 3),
            row("8 * 3 + 12 * (4 - 2)", 48),
            row("1 +++ 2 * 3 -- 4", 11),
            row("a*2+b*3+c*(2+3)", 53)
        ) { input, result ->
            calculator.evaluateComputation(input) shouldBe result
        }
    }
})
