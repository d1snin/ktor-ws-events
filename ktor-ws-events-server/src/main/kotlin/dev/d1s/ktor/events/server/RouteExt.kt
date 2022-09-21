/*
 * Copyright 2022 Mikhail Titov
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

import dev.d1s.ktor.events.commons.EventSource
import dev.d1s.ktor.events.commons.WsEvent
import dev.d1s.ktor.events.commons.WsEventSendingConnection
import dev.d1s.ktor.events.commons.constant.DEFAULT_EVENTS_ROUTE
import dev.d1s.ktor.events.commons.constant.GROUP_PATH_PARAMETER
import dev.d1s.ktor.events.commons.constant.GROUP_SEGMENT_PLACEHOLDER
import dev.d1s.ktor.events.commons.constant.PRINCIPAL_QUERY_PARAMETER
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

public fun Route.deployWsEventPublisher(
    route: String = DEFAULT_EVENTS_ROUTE,
    channel: Channel<WsEvent<*>> = Channel(),
    eventReceivingScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
): WsEventPublisher {
    require(route.contains(GROUP_SEGMENT_PLACEHOLDER)) {
        "Group segment ($GROUP_SEGMENT_PLACEHOLDER) placeholder must be present."
    }

    val connections = CopyOnWriteArrayList<WsEventSendingConnection>()

    webSocket(route) {
        val eventSource = EventSource(
            call.parameters[GROUP_PATH_PARAMETER] ?: error("Group parameter is not present."),
            call.request.queryParameters[PRINCIPAL_QUERY_PARAMETER]
        )

        connections += WsEventSendingConnection(eventSource, this)
    }

    eventReceivingScope.launch {
        for (event in channel) {
            val connection = connections.find {
                it.eventSource == event.source
            }

            val session = connection?.session as? WebSocketServerSession

            session?.sendSerialized(event)
        }
    }

    return channel
}