package dev.d1s.ktor.events.server

import dev.d1s.ktor.events.server.dto.WebSocketEventDto
import dev.d1s.ktor.events.server.entity.EventSendingConnection
import dev.d1s.ktor.events.server.entity.ServerWebSocketEvent

/**
 * Final filter for outgoing events.
 * [WebSocketEventDto] is sent to client only when this [predicate] returns true. Otherwise, the event is ignored for current connection.
 */
public fun interface OutgoingEventFilter {

    public suspend fun predicate(
        event: ServerWebSocketEvent,
        dto: WebSocketEventDto,
        connection: EventSendingConnection
    ): Boolean
}