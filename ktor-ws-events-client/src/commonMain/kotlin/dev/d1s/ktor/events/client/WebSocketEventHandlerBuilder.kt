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

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.util.Routes
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.diamondedge.logging.logging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val logger = logging()

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
    path: String = Routes.DEFAULT_EVENTS_ROUTE,
    block: suspend DefaultClientWebSocketSession.() -> Unit
) {
    checkPluginInstalled()

    val webSocketEventsConfiguration = attributes.webSocketEventsConfiguration

    val requestConfiguration: HttpRequestBuilder.() -> Unit = {
        parameter(Routes.PRINCIPAL_QUERY_PARAMETER, reference.principal)

        val parameters = Parameters.build {
            reference.parameters.forEach { (key, value) ->
                append(key, value)
            }
        }

        url.parameters.appendMissing(parameters)

        header(Routes.CLIENT_ID_HEADER, webSocketEventsConfiguration.clientId)
    }

    val url = URLBuilder(webSocketEventsConfiguration.requiredBaseUrl).apply {
        val configuredPath = path.replace(Routes.GROUP_SEGMENT_PLACEHOLDER, reference.group)
        path(configuredPath)
    }.buildString()

    logger.d {
        "Will start WS session at $url"
    }

    reconnecting(url, webSocketEventsConfiguration) { onConnected ->
        webSocket(
            urlString = url,
            request = requestConfiguration
        ) {
            onConnected()
            block()
        }
    }
}

/**
 * Runs [session] forever, reconnecting after it fails or ends with an
 * exponential, jittered backoff (see [ReconnectBackoff]). The backoff starts
 * over after a session that stayed open for
 * [WebSocketEventsConfiguration.stableSessionDuration]. A failure streak is
 * logged at WARN on its first attempt and every [WARN_EVERY_ATTEMPTS] attempts
 * after that (stack traces at DEBUG only), and its end at INFO.
 */
private suspend fun reconnecting(
    url: String,
    configuration: WebSocketEventsConfiguration,
    session: suspend (onConnected: () -> Unit) -> Unit
) {
    val backoff = ReconnectBackoff(configuration.reconnectInitialDelay, configuration.reconnectMaxDelay)

    while (true) {
        var connectedAt: TimeMark? = null

        val error = try {
            session {
                connectedAt = TimeSource.Monotonic.markNow()

                if (backoff.attempts > 0) {
                    logger.i {
                        "WS session at $url re-established after ${backoff.attempts} reconnect attempt(s)"
                    }
                }
            }

            null
        } catch (e: CancellationException) {
            // a real cancellation of the listener stops it; anything else
            // shaped like one (a closed session) is a plain failure
            if (!currentCoroutineContext().isActive) throw e

            e
        } catch (e: Throwable) {
            e
        }

        val lasted = connectedAt?.elapsedNow()

        if (lasted != null && lasted >= configuration.stableSessionDuration) {
            backoff.reset()
        }

        val wait = backoff.next()
        val attempt = backoff.attempts
        val reason = error?.let { "${it::class.simpleName}: ${it.message}" } ?: "closed by the server"

        if (attempt == 1 || attempt % WARN_EVERY_ATTEMPTS == 0) {
            logger.w {
                if (attempt == 1) {
                    "WS session at $url ended ($reason); reconnecting in $wait"
                } else {
                    "WS session at $url still down after $attempt attempts ($reason); next attempt in $wait"
                }
            }
        } else {
            logger.d {
                "WS session at $url attempt $attempt failed ($reason); next attempt in $wait"
            }
        }

        if (error != null) {
            logger.d {
                error.stackTraceToString()
            }
        }

        delay(wait)
    }
}

private const val WARN_EVERY_ATTEMPTS = 20

private fun HttpClient.checkPluginInstalled() {
    pluginOrNull(WebSocketEvents) ?: error("WebSocketEvents plugin is not installed.")
}