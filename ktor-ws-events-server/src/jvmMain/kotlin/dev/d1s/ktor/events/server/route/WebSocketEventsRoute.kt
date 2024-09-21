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

package dev.d1s.ktor.events.server.route

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.util.Routes
import dev.d1s.ktor.events.server.WebSocketEvents
import dev.d1s.ktor.events.server.entity.EventSendingConnection
import dev.d1s.ktor.events.server.util.eventProcessor
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.lighthousegames.logging.logging

private val log = logging()

/**
 * Installs a WebSocket route to your application which is
 * supposed to propagate events to clients.
 * **This function is supposed to be called once.**
 *
 * @throws IllegalArgumentException if the provided route does not container group segment placeholder.
 * @throws IllegalStateException if the application does not have [WebSocketEvents] plugin installed.
 * @see WebSocketEvents
 */
public fun Route.webSocketEvents(
    route: String = Routes.DEFAULT_EVENTS_ROUTE,
    preprocess: suspend DefaultWebSocketServerSession.(EventReference) -> Unit = {}
) {
    log.d {
        "Exposing route $route"
    }

    require(route.contains(Routes.GROUP_SEGMENT_PLACEHOLDER)) {
        "Group segment placeholder ${Routes.GROUP_SEGMENT_PLACEHOLDER} must be present."
    }

    application.checkPluginInstalled()

    val processor = application.attributes.eventProcessor

    webSocket(route) {
        log.d {
            "Handled WS session"
        }

        val queryParameters = call.request.queryParameters

        val parameters = buildMap {
            queryParameters.forEach { key, values ->
                if (key != Routes.PRINCIPAL_QUERY_PARAMETER) {
                    put(key, values.first())
                }
            }
        }

        log.d {
            "Parameters: $parameters"
        }

        val eventReference = EventReference(
            call.parameters[Routes.GROUP_PATH_PARAMETER] ?: error("Group parameter is not present."),
            queryParameters[Routes.PRINCIPAL_QUERY_PARAMETER],
            parameters
        )

        log.d {
            "Extracted event reference: $eventReference"
        }

        preprocess(eventReference)

        val connection = EventSendingConnection(eventReference, this, call)
        processor.process(connection)

        receive()
    }
}

private fun Application.checkPluginInstalled() {
    pluginOrNull(WebSocketEvents) ?: error("WebSocketEvents plugin is not installed on this application.")
}

private suspend fun DefaultWebSocketServerSession.receive() {
    while (true) {
        val data = incoming.receiveCatching().getOrNull()
        data ?: log.d {
            "Client connection lost"
        }
        data ?: break
    }
}