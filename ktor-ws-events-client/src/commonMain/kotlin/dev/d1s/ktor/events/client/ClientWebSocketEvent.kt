package dev.d1s.ktor.events.client

import dev.d1s.ktor.events.commons.AbstractEvent
import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.commons.UnixTime
import kotlinx.serialization.Serializable

@Serializable
public data class ClientWebSocketEvent<T>(
    override val id: Identifier,
    override val reference: EventReference,
    override val initiated: UnixTime,
    val data: T
) : AbstractEvent