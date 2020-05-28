package org.kata

import java.util.*

data class ParseException(val reason: String) : Exception(reason)

class Calculator {
    private val assignmentRegex = """(\w+)=(-?\w+)""".toRegex()
    private val numberRegex = """-?\d+""".toRegex()
    private val variables = mutableMapOf<String, Int>()

    fun promptCompute(scanner: Scanner) {
        var line = scanner.nextLine().replace(" ", "")
        while (line != "/exit") {
            if (line.isNotBlank()) {
                when {
                    line == "/help" -> println("The program calculates the sum of numbers")
                    line.first() == '/' -> println("Unknown command")
                    line.contains('=') -> evaluateAssignment(line)
                    line.matches(numberRegex) -> println(line)
                    line.matches("""^([a-zA-Z]+)$""".toRegex()) -> variables[line]?.let { println(it) } ?: throw ParseException("Unknown variable")
                    else -> {
                        println(try {
                            evaluateComputation(line)
                        } catch (ex: Exception) {
                            ex.message
                        })
                    }
                }
            }
            line = scanner.nextLine().replace(" ", "")
        }
    }

    fun evaluateAssignment(line: String) {
        assignmentRegex.matchEntire(line)?.let { result ->
            val left = result.groupValues[1]
            val right = result.groupValues[2]
            if (left.any { !it.isLetter() }) {
                throw ParseException("Invalid identifier")
            }
            variables[left] = if (right.matches(numberRegex)) {
                right.toInt()
            } else {
                if (right.any { !it.isLetter() }) {
                    throw ParseException("Invalid assignment")
                }
                variables[right] ?: throw ParseException("Unknown variable")
            }
        } ?: throw ParseException("Invalid assignment")
    }

    fun evaluateComputation(line: String): Int {
        return ComputationParser.parse(line)
            .let { InfixToPostfixConverter.convert(it) }
            .let { calculate(it) }
    }

    private fun applyOnStack(stack: List<Int>, element: ComputationElement): List<Int> {
        return when (element) {
            is ComputationNumber -> listOf(element.value) + stack
            is ComputationVariable -> {
                val value = variables[element.variable] ?: throw ParseException("Unknown variable")
                listOf(value) + stack
            }
            is ComputationOperator -> {
                listOf(element.combine(stack[1], stack[0])) + stack.drop(2)
            }
        }
    }

    private fun calculate(elements: List<ComputationElement>): Int {
        return elements.fold(listOf(), ::applyOnStack).first()
    }
}

object InfixToPostfixConverter {

    // see http://www.cs.nthu.edu.tw/~wkhon/ds/ds10/tutorial/tutorial2.pdf
    fun convert(elements: List<ParserElement>): List<ComputationElement> {
        val result = mutableListOf<ComputationElement>()
        val stack = ArrayDeque<ParserElement>()
        elements.forEach {
            when (it) {
                is ComputationNumber -> result.add(it)
                is ComputationVariable -> result.add(it)
                is ComputationOperator -> {
                    while (stack.isNotEmpty()
                        && precedence(it) <= precedence(stack.peek())
                        && stack.peek() != LeftParenthesis
                    ) {
                        result.add(stack.pop() as ComputationElement)
                    }
                    stack.push(it)
                }
                is LeftParenthesis -> stack.push(it)
                is RightParenthesis -> {
                    while (stack.isNotEmpty() && stack.peek() != LeftParenthesis) {
                        result.add(stack.pop() as ComputationElement)
                    }
                    if (stack.isEmpty() && stack.peek() != LeftParenthesis)
                        throw ParseException("Invalid expression")
                    else
                        stack.pop();

                }
            }
        }

        while (stack.isNotEmpty()) {
            if (stack.peek() is LeftParenthesis) {
                throw ParseException("Invalid expression")
            }
            result.add(stack.pop() as ComputationElement)
        }
        return result
    }

    private fun precedence(elmt: ParserElement) = when (elmt) {
        is ComputationOperator -> elmt.operator.priority
        else -> -1
    }
}