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

package dev.d1s.ktor.events.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.websocket.*

public val WebSocketEvents: ClientPlugin<WebSocketEventsConfiguration> =
    createClientPlugin("websocket-events", ::WebSocketEventsConfiguration) {
        pluginConfig.validate()

        client.checkWebSocketsPluginInstalled()

        client.attributes.webSocketEventsConfiguration = pluginConfig
    }

public class WebSocketEventsConfiguration {

    public var url: String? = null

    internal val requiredBaseUrl
        get() = requireNotNull(url) {
            "URL is not configured."
        }

    internal fun validate() {
        requiredBaseUrl
    }
}

private fun HttpClient.checkWebSocketsPluginInstalled() {
    pluginOrNull(WebSockets) ?: error("WebSockets plugin is not installed.")
}