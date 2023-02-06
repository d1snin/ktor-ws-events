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
import java.util.concurrent.CopyOnWriteArrayList

internal typealias WebSocketEventReceiver = ReceiveChannel<WebSocketEvent<*>>

internal interface WebSocketEventConsumer {

    fun launch(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver)

    fun addConnection(connection: WebSocketEventSendingConnection)
}

internal class DefaultWebSocketEventConsumer : WebSocketEventConsumer {

    private val connections = CopyOnWriteArrayList<WebSocketEventSendingConnection>()

    override fun launch(eventReceivingScope: CoroutineScope, channel: WebSocketEventReceiver) {
        eventReceivingScope.launch {
            for (event in channel) {
                val connection = findConnection(event.reference)

                connection.sendEvent(event)
            }
        }
    }

    override fun addConnection(connection: WebSocketEventSendingConnection) {
        connections += connection
    }

    private fun findConnection(reference: EventReference) =
        connections.find {
            reference == it.reference
        }

    private suspend fun WebSocketEventSendingConnection?.sendEvent(event: WebSocketEvent<*>) {
        val session = this?.session as? WebSocketServerSession

        session?.sendSerialized(event)
    }
}