package dev.d1s.ktor.events.server

import dev.d1s.ktor.events.commons.EventReference

internal data class WebSocketEventDto(
    val reference: EventReference,
    val data: Any
)
