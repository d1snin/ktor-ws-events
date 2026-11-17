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

package dev.d1s.ktor.events

import dev.d1s.ktor.events.client.WebSocketEvents
import dev.d1s.ktor.events.client.webSocketEvents
import dev.d1s.ktor.events.commons.ref
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// A server that refuses the upgrade (503, like a restarting backend) must be
// retried with a growing backoff, not hammered: over 8 s the old fixed 300 ms
// delay made ~26 attempts; 500 ms doubling makes 4-5.
class ReconnectBackoffTest {

    @Test
    fun refusedSessionsBackOff() = runBlocking {
        val attempts = AtomicInteger()

        val server = embeddedServer(Netty, port = PORT) {
            routing {
                get("{...}") {
                    attempts.incrementAndGet()
                    call.respond(HttpStatusCode.ServiceUnavailable)
                }
            }
        }.start(wait = false)

        val client = HttpClient(CIO) {
            install(WebSockets)

            install(WebSocketEvents) {
                url = "ws://localhost:$PORT"
                reconnectInitialDelay = 500.milliseconds
                reconnectMaxDelay = 30.seconds
            }
        }

        try {
            val listener = launch(Dispatchers.IO) {
                client.webSocketEvents(ref("backoff")) {}
            }

            delay(8.seconds)
            listener.cancelAndJoin()

            val made = attempts.get()
            assertTrue(made in 3..6, "expected 3..6 attempts in 8 s with backoff, got $made")
        } finally {
            client.close()
            server.stop(100, 500)
        }
    }

    private companion object {
        private const val PORT = 9181
    }
}
