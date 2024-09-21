package dev.d1s.ktor.events.commons

public typealias Identifier = String

private val chars = ('0'..'9') + ('a'..'z')

public val randomId: Identifier
    get() = chars.shuffled().take(16).joinToString("")