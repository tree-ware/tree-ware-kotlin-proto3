package org.treeWare.proto3.message

fun snakeToCamel(snake: String): String =
    snake.split("_").joinToString("", transform = ::capitalize)

fun snakeToLowerCamel(snake: String): String =
    uncapitalize(snakeToCamel(snake))

fun capitalize(lower: String): String = lower.replaceFirstChar { it.uppercase() }

fun uncapitalize(upper: String): String = upper.replaceFirstChar { it.lowercase() }