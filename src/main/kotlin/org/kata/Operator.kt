package org.kata

enum class Operator(val symbol: Char, val priority: Int, val combine: (Int, Int) -> Int) {
    PLUS('+', 1, combine = { a, b -> a + b }),
    MINUS('-', 1, combine = { a, b -> a - b }),
    MULTIPLY('*', 2, combine = { a, b -> a * b }),
    DIVIDE('/', 2, combine = { a, b -> a / b });

    companion object {
        val allOperatorSymbols: Map<Char, Operator> = values().map { it.symbol to it}.toMap()
    }
}
