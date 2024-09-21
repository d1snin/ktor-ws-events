package dev.d1s.ktor.events.server.dto

import dev.d1s.ktor.events.commons.AbstractEvent
import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.commons.UnixTime

internal data class WebSocketEventDto(
    override val id: Identifier,
    override val reference: EventReference,
    override val initiated: UnixTime,
    val data: Any
) : AbstractEvent
