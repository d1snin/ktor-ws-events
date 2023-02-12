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

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.WebSocketEvent
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging
import java.util.concurrent.CopyOnWriteArrayList

internal typealias WebSocketEventReceiver = ReceiveChannel<WebSocketEvent<*>>

internal interface WebSocketEventConsumer {

    fun launch(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver)

    fun addConnection(connection: WebSocketEventSendingConnection)
}

internal class DefaultWebSocketEventConsumer : WebSocketEventConsumer {

    private val connections = CopyOnWriteArrayList<WebSocketEventSendingConnection>()

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

                val connection = findConnection(event.reference)

                connection.sendEvent(event)
            }
        }
    }

    override fun addConnection(connection: WebSocketEventSendingConnection) {
        connections += connection

        log.d {
            "Added connection with reference: ${connection.reference}. Connections: $connections"
        }
    }

    private fun findConnection(reference: EventReference): WebSocketEventSendingConnection? {
        log.v {
            "Finding connection in $connections"
        }

        val connection = connections.find {
            it.reference == reference
        }

        log.d {
            "Found connection $connection. Wanted a connection with reference $reference"
        }

        return connection
    }

    private suspend fun WebSocketEventSendingConnection?.sendEvent(event: WebSocketEvent<*>) {
        log.d {
            "Sending event..."
        }

        val session = this?.session as? WebSocketServerSession

        session?.sendSerialized(event)
    }
}