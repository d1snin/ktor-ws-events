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
import dev.d1s.ktor.events.commons.util.Routes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.channels.getOrElse
import org.lighthousegames.logging.logging

private val log = logging()

/**
 * Installs a WebSocket route to your application which is
 * supposed to propagate [WebSocketEvents][dev.d1s.ktor.events.commons.WebSocketEvent] to clients.
 * **This function is supposed to be called once.**
 *
 * @throws IllegalArgumentException if the provided route does not container group segment placeholder.
 * @throws IllegalStateException if the application does not have [WebSocketEvents] plugin installed.
 * @see WebSocketEvents
 */
public fun Route.webSocketEvents(route: String = Routes.DEFAULT_EVENTS_ROUTE) {
    log.d {
        "Exposing route $route"
    }

    require(route.contains(Routes.GROUP_SEGMENT_PLACEHOLDER)) {
        "Group segment placeholder ${Routes.GROUP_SEGMENT_PLACEHOLDER} must be present."
    }

    application.checkPluginInstalled()

    val consumer = application.attributes.webSocketEventConsumer

    webSocket(route) {
        log.d {
            "Handled WS session"
        }

        val eventReference = EventReference(
            call.parameters[Routes.GROUP_PATH_PARAMETER] ?: error("Group parameter is not present."),
            call.request.queryParameters[Routes.PRINCIPAL_QUERY_PARAMETER]
        )

        log.d {
            "Extracted event reference: $eventReference"
        }

        val connection = WebSocketEventSendingConnection(eventReference, this)

        consumer.addConnection(connection)

        var receiving = true

        while (receiving) {
            incoming.receiveCatching().getOrElse {
                log.w {
                    "Failed to receive"
                }

                it?.printStackTrace()

                consumer.removeConnection(connection)

                receiving = false
            }
        }
    }
}

private fun Application.checkPluginInstalled() {
    pluginOrNull(WebSocketEvents) ?: error("WebSocketEvents plugin is not installed on this application.")
}