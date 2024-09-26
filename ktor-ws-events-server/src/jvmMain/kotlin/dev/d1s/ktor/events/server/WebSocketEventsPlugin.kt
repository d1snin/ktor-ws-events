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

package dev.d1s.ktor.events.server

import dev.d1s.ktor.events.server.pool.EventPool
import dev.d1s.ktor.events.server.util.eventPool
import dev.d1s.ktor.events.server.util.eventProcessor
import dev.d1s.ktor.events.server.util.filter
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import org.lighthousegames.logging.logging

internal const val WEBSOCKET_EVENTS_PLUGIN_NAME = "websocket-events"

private val log = logging()

/**
 * Enables support for event streaming over WebSockets.
 * Supposed to be used in pair with [dev.d1s.ktor.events.server.route.webSocketEvents] route builder.
 *
 * Example usage:
 * ```kotlin
 * val pool = InMemoryEventPool()
 *
 * install(WebSocketEvents) {
 *     eventPool = pool
 * }
 *
 * routing {
 *     webSocketEvents()
 * }
 *
 * val createdBook = createBook()
 * val reference = ref("book_created")
 * val event = event(reference, createdBook)
 *
 * pool.push(event)
 * ```
 * @see WebSocketEventsConfiguration
 * @see EventPool
 */
public val WebSocketEvents: ApplicationPlugin<WebSocketEventsConfiguration> =
    createApplicationPlugin(WEBSOCKET_EVENTS_PLUGIN_NAME, ::WebSocketEventsConfiguration) {
        log.d {
            "Installing WebSocketEvents plugin"
        }

        if (!application.hasWebSocketsPlugin()) {
            application.installWebSockets()
        }

        application.attributes.eventProcessor = DefaultEventProcessor()
        application.attributes.eventPool = pluginConfig.eventPool ?: error("Event pool must be specified")
        application.attributes.filter = pluginConfig.filter
    }

public class WebSocketEventsConfiguration {

    public var eventPool: EventPool? = null

    public var filter: OutgoingEventFilter? = null
}

private fun Application.hasWebSocketsPlugin() = pluginOrNull(WebSockets) != null

private fun Application.installWebSockets() {
    install(WebSockets) {
        contentConverter = JacksonWebsocketContentConverter()
    }
}