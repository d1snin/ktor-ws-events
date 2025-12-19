/*
 * Copyright 2022-2024 Mikhail Titov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.d1s.ktor.events.server

import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.server.dto.WebSocketEventDto
import dev.d1s.ktor.events.server.entity.EventSendingConnection
import dev.d1s.ktor.events.server.entity.ServerWebSocketEvent
import dev.d1s.ktor.events.server.pool.EventPool
import dev.d1s.ktor.events.server.util.eventPool
import dev.d1s.ktor.events.server.util.filter
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

internal interface EventProcessor {

    fun process(connection: EventSendingConnection)
}

internal class DefaultEventProcessor : EventProcessor {

    private val log = logging()

    private val eventProcessorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun process(connection: EventSendingConnection) {
        eventProcessorScope.launch {
            val reference = connection.reference
            val call = connection.call
            val pool = call.application.attributes.eventPool
            val filter = call.application.attributes.filter
            val client = connection.clientId

            log.d {
                "Processing events for reference: $reference"
            }

            val unreceived = pool.get(reference, client)

            log.d {
                "Sending previously unreceived events: $unreceived"
            }

            connection.sendAll(pool, filter, client, unreceived)

            pool.onEvent(reference, client) {
                log.d {
                    "Got event from event channel: $it"
                }

                connection.sendEvent(pool, filter, client, it)
            }
        }
    }

    private suspend fun EventSendingConnection.sendAll(
        pool: EventPool,
        filter: OutgoingEventFilter?,
        client: Identifier,
        events: List<ServerWebSocketEvent>
    ) {
        events.forEach {
            sendEvent(pool, filter, client, it)
        }
    }

    private suspend fun EventSendingConnection.sendEvent(
        pool: EventPool,
        filter: OutgoingEventFilter?,
        client: Identifier,
        event: ServerWebSocketEvent
    ) {
        log.d {
            "Sending event... Connection: $this"
        }

        (session as? WebSocketServerSession)?.let { session ->
            processEvent(pool, filter, client, session, event)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun EventSendingConnection.processEvent(
        pool: EventPool,
        filter: OutgoingEventFilter?,
        client: Identifier,
        session: WebSocketServerSession,
        event: ServerWebSocketEvent
    ) {
        if (!session.outgoing.isClosedForSend) {
            val dto = WebSocketEventDto(
                id = event.id,
                reference = reference,
                initiated = event.initiated,
                data = event.dataSupplier(reference.parameters)
            )

            val filterResolution = filter?.predicate(event, dto, this) ?: true

            if (filterResolution) {
                session.sendSerialized(dto)
                pool.confirm(event.id, client)
            }
        } else {
            log.d {
                "Couldn't send event. Connection is closed."
            }
        }
    }
}