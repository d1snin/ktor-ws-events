/*
 * Copyright 2022-2023 Mikhail Titov
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

import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

internal typealias WebSocketEventReceiver = ReceiveChannel<ServerWebSocketEvent>

internal interface WebSocketEventConsumer {

    fun launch(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver)

    fun addConnection(connection: WebSocketEventSendingConnection)

    fun removeConnection(connection: WebSocketEventSendingConnection)
}

internal class DefaultWebSocketEventConsumer : WebSocketEventConsumer {

    private val connectionPool = webSocketEventSendingConnectionPool()

    private val log = logging()

    override fun launch(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver) {
        log.d {
            "Launching WebSocket event consumer..."
        }

        eventReceivingScope.launch {
            handleEvents(eventReceivingScope, channel)
        }
    }

    override fun addConnection(connection: WebSocketEventSendingConnection) {
        connectionPool += connection
    }

    override fun removeConnection(connection: WebSocketEventSendingConnection) {
        connectionPool -= connection
    }

    private suspend fun handleEvents(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver) {
        for (event in channel) {
            log.d {
                "Consumed event $event"
            }

            processEvent(eventReceivingScope, event)
        }
    }

    private fun processEvent(eventReceivingScope: CoroutineScope, event: ServerWebSocketEvent) {
        val connections = connectionPool[event.reference]

        connections.parallelStream().forEach { connection ->
            eventReceivingScope.launch {
                connection.sendEvent(event)
            }
        }
    }

    private suspend fun WebSocketEventSendingConnection.sendEvent(event: ServerWebSocketEvent) {
        log.d {
            "Sending event... Connection: $this"
        }

        (session as? WebSocketServerSession)?.let { session ->
            processSession(session, event)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun WebSocketEventSendingConnection.processSession(
        session: WebSocketServerSession,
        event: ServerWebSocketEvent
    ) {
        if (!session.outgoing.isClosedForSend) {
            sendEventDto(session, event)
        } else {
            log.d {
                "Couldn't send event. Connection is closed."
            }

            connectionPool -= reference
        }
    }

    private suspend fun WebSocketEventSendingConnection.sendEventDto(
        session: WebSocketServerSession,
        event: ServerWebSocketEvent
    ) {
        val dto = WebSocketEventDto(
            reference = reference,
            data = event.dataSupplier(reference.parameters)
        )

        session.sendSerialized(dto)
    }
}