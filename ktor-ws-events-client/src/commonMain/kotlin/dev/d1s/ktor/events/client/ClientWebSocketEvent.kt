package dev.d1s.ktor.events.client

import dev.d1s.ktor.events.commons.EventReference
import kotlinx.serialization.Serializable

@Serializable
public data class ClientWebSocketEvent<T>(
    val reference: EventReference,
    val data: T
)
