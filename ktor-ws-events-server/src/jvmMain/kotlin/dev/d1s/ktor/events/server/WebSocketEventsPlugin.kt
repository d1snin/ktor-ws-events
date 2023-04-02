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

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.lighthousegames.logging.logging

internal const val WEBSOCKET_EVENTS_PLUGIN_NAME = "websocket-events"

private val log = logging()

/**
 * Enables support for event streaming over WebSockets.
 * Supposed to be used in pair with [webSocketEvents] route builder.
 * Once the plugin is installed, it will fire a job listening for events
 * in the provided [WebSocketEventsConfiguration.channel].
 *
 * Example usage:
 * ```kotlin
 * val eventChannel = WebSocketEventChannel()
 *
 * install(WebSocketEvents) {
 *     channel = eventChannel
 * }
 *
 * routing {
 *     webSockets()
 * }
 *
 * val createdBook = createBook()
 * val reference = ref("book_created")
 * val event = event(reference, createdBook)
 *
 * eventChannel.send(event)
 * ```
 * @see webSocketEvents
 * @see WebSocketEventChannel
 * @see WebSocketEventsConfiguration
 */
public val WebSocketEvents: ApplicationPlugin<WebSocketEventsConfiguration> =
    createApplicationPlugin(WEBSOCKET_EVENTS_PLUGIN_NAME, ::WebSocketEventsConfiguration) {
        log.d {
            "Installing WebSocketEvents plugin"
        }

        val eventReceivingScope = pluginConfig.eventReceivingScope
        val channel = pluginConfig.requiredChannel

        if (!application.hasWebSocketsPlugin()) {
            application.installWebSockets()
        }

        val webSocketEventConsumer = webSocketEventConsumer().apply {
            application.attributes.webSocketEventConsumer = this
        }

        webSocketEventConsumer.launch(eventReceivingScope, channel)
    }

public class WebSocketEventsConfiguration {

    public var channel: WebSocketEventChannel? = null

    public var eventReceivingScope: CoroutineScope = defaultEventReceivingScope()

    internal val requiredChannel
        get() = requireNotNull(channel) {
            "channel is not configured."
        }
}

private fun defaultEventReceivingScope() = CoroutineScope(Dispatchers.IO)

private fun Application.hasWebSocketsPlugin() = pluginOrNull(WebSockets) != null

private fun Application.installWebSockets() {
    install(WebSockets) {
        contentConverter = JacksonWebsocketContentConverter()
    }
}