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
import dev.d1s.ktor.events.commons.WebSocketEventSendingConnection
import dev.d1s.ktor.events.commons.util.Routes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

public fun Route.webSocketEvents(route: String = Routes.DEFAULT_EVENTS_ROUTE) {
    require(route.contains(Routes.GROUP_SEGMENT_PLACEHOLDER)) {
        "Group segment placeholder ${Routes.GROUP_SEGMENT_PLACEHOLDER} must be present."
    }

    application.checkPluginInstalled()

    val consumer = application.attributes.webSocketEventConsumer

    webSocket(route) {
        val eventReference = EventReference(
            call.parameters[Routes.GROUP_PATH_PARAMETER] ?: error("Group parameter is not present."),
            call.request.queryParameters[Routes.PRINCIPAL_QUERY_PARAMETER]
        )

        val connection = WebSocketEventSendingConnection(eventReference, this)

        consumer.addConnection(connection)
    }
}

private fun Application.checkPluginInstalled() {
    pluginOrNull(WebSocketEvents) ?: error("WebSocketEvents plugin is not installed on this application.")
}