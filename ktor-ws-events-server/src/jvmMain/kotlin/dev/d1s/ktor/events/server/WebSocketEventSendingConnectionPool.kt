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
import org.lighthousegames.logging.logging
import java.util.concurrent.CopyOnWriteArrayList

internal interface WebSocketEventSendingConnectionPool {

    operator fun plusAssign(connection: WebSocketEventSendingConnection)

    operator fun minusAssign(connection: WebSocketEventSendingConnection)

    operator fun get(reference: EventReference): WebSocketEventSendingConnection?

    operator fun minusAssign(reference: EventReference)
}

internal class DefaultWebSocketEventSendingConnectionPool : WebSocketEventSendingConnectionPool {

    private val connections = CopyOnWriteArrayList<WebSocketEventSendingConnection>()

    private val log = logging()

    override fun plusAssign(connection: WebSocketEventSendingConnection) {
        connections += connection

        log.d {
            "Added connection with reference: ${connection.reference}. Connections: $connections"
        }
    }

    override fun minusAssign(connection: WebSocketEventSendingConnection) {
        connections -= connection

        log.d {
            "Removed connection with reference: ${connection.reference}. Connections: $connections"
        }
    }

    override fun minusAssign(reference: EventReference) {
        this[reference]?.let {
            this -= it
        }
    }

    override fun get(reference: EventReference): WebSocketEventSendingConnection? {
        log.v {
            "Finding connection in $connections by reference $reference"
        }

        val connection = connections.find {
            it.reference == reference
        }

        log.d {
            "Found connection $connection. Wanted a connection with reference $reference"
        }

        return connection
    }
}