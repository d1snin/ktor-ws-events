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

import io.ktor.server.application.*
import io.ktor.util.*

internal object Key {

    internal val WebSocketEventConsumer =
        AttributeKey<WebSocketEventConsumer>(name("websocket-event-consumer"))

    internal val WebSocketEventSendingConnectionPool =
        AttributeKey<WebSocketEventSendingConnectionPool>(name("websocket-event-sending-connection-pool"))

    private fun name(component: String) = "${WEBSOCKET_EVENTS_PLUGIN_NAME}_$component"
}

internal fun webSocketEventConsumer(application: Application) =
    DefaultWebSocketEventConsumer(application)

internal fun webSocketEventSendingConnectionPool() =
    DefaultWebSocketEventSendingConnectionPool()

internal var Attributes.webSocketEventConsumer: WebSocketEventConsumer
    get() = this[Key.WebSocketEventConsumer]
    set(value) = this.put(Key.WebSocketEventConsumer, value)

internal var Attributes.webSocketEventSendingConnectionPool: WebSocketEventSendingConnectionPool
    get() = this[Key.WebSocketEventSendingConnectionPool]
    set(value) = this.put(Key.WebSocketEventSendingConnectionPool, value)