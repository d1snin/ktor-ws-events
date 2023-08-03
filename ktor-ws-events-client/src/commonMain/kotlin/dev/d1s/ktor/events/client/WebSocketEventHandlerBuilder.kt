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

package dev.d1s.ktor.events.client

import dev.d1s.ktor.events.commons.EventGroup
import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.util.Routes
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Opens a [block] with [DefaultClientWebSocketSession] associated with the given [event reference][reference] and optional [path].
 *
 * Example usage:
 * ```kotlin
 * client.webSocketEvents(ref("book_updated", "<book_id>")) {
 *     val event: WebSocketEvent<Book> = receiveWebSocketEvent()
 *
 *     println(event.data.author)
 * }
 * ```
 * @throws IllegalStateException if [WebSocketEvents] plugin is not installed.
 * @see receiveWebSocketEvent
 * @see dev.d1s.ktor.events.commons.ref
 * @see WebSocketEvents
 */
public suspend fun HttpClient.webSocketEvents(
    reference: EventReference,
    path: String = makeDefaultEventsRoute(reference.group),
    block: suspend DefaultClientWebSocketSession.() -> Unit
) {
    checkPluginInstalled()

    val webSocketEventsConfiguration = attributes.webSocketEventsConfiguration

    val requestConfiguration: HttpRequestBuilder.() -> Unit = {
        parameter(Routes.PRINCIPAL_QUERY_PARAMETER, reference.principal)
    }

    val url = URLBuilder(webSocketEventsConfiguration.requiredBaseUrl).apply {
        path(path)
    }.buildString()

    webSocket(
        urlString = url,
        request = requestConfiguration,
        block = block
    )
}

private fun makeDefaultEventsRoute(group: EventGroup) =
    Routes.DEFAULT_EVENTS_ROUTE.replace(Routes.GROUP_SEGMENT_PLACEHOLDER, group)

private fun HttpClient.checkPluginInstalled() {
    pluginOrNull(WebSocketEvents) ?: error("WebSocketEvents plugin is not installed.")
}