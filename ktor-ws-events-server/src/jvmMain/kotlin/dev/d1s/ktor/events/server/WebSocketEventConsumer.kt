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
            for (event in channel) {
                log.d {
                    "Consumed event $event"
                }

                val connection = connectionPool[event.reference]

                connection.sendEvent(event)
            }
        }
    }

    override fun addConnection(connection: WebSocketEventSendingConnection) {
        connectionPool += connection
    }

    override fun removeConnection(connection: WebSocketEventSendingConnection) {
        connectionPool -= connection
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun WebSocketEventSendingConnection?.sendEvent(event: ServerWebSocketEvent) {
        log.d {
            "Sending event... Connection: $this"
        }

        (this?.session as? WebSocketServerSession)?.let { session ->
            if (!session.outgoing.isClosedForSend) {
                val dto = WebSocketEventDto(
                    reference = reference,
                    data = event.dataSupplier(reference.parameters)
                )

                session.sendSerialized(dto)
            } else {
                log.d {
                    "Couldn't send event. Connection is closed."
                }

                connectionPool -= reference
            }
        }
    }
}