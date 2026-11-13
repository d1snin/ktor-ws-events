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

import dev.d1s.ktor.events.client.ClientWebSocketEvent
import dev.d1s.ktor.events.client.receiveWebSocketEvent
import dev.d1s.ktor.events.client.webSocketEvents
import dev.d1s.ktor.events.commons.ClientParameters
import dev.d1s.ktor.events.configuration.pool
import dev.d1s.ktor.events.configuration.runTestServer
import dev.d1s.ktor.events.configuration.webSocketClient
import dev.d1s.ktor.events.server.entity.event
import io.ktor.server.engine.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import com.diamondedge.logging.logging
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val EVENT_DELIVERY_TIMEOUT = 30_000L

class EventDeliveryTest {

    private val log = logging()

    private lateinit var server: EmbeddedServer<*, *>

    @BeforeTest
    fun prepareServer() {
        server = runTestServer()
    }

    @AfterTest
    fun stopServer() {
        server.stop()
    }

    @Test
    fun `server must deliver an event`() {
        runBlocking {
            val receivedEvent = CompletableDeferred<ClientWebSocketEvent<TestEventData>>()
            val receivedParameters = CompletableDeferred<ClientParameters>()

            // webSocketEvents() reconnects forever (even after the block returns),
            // so the listener has to be cancelled explicitly once the event arrives.
            // Assertions are made outside the block: errors thrown inside it are
            // swallowed by the client's retry loop.
            val listener = launch {
                webSocketClient.webSocketEvents(testClientEventReference) {
                    val event = receiveWebSocketEvent<TestEventData>()

                    log.i {
                        "Received test event: $event"
                    }

                    receivedEvent.complete(event)
                }
            }

            try {
                delay(1_000)

                sendTestEvent(receivedParameters)

                val event = withTimeout(EVENT_DELIVERY_TIMEOUT) {
                    receivedEvent.await()
                }

                assertEquals(testServerEventReference, event.reference)
                assertEquals(testEventData.message, event.data.message)

                val parameters = withTimeout(EVENT_DELIVERY_TIMEOUT) {
                    receivedParameters.await()
                }

                assertEquals(TEST_CLIENT_PARAMETER_DATA, parameters[TEST_CLIENT_PARAMETER_KEY])
            } finally {
                listener.cancelAndJoin()
            }
        }
    }

    private suspend fun sendTestEvent(receivedParameters: CompletableDeferred<ClientParameters>) {
        val event = event(testServerEventReference) { parameters ->
            log.i {
                "Got parameters: $parameters"
            }

            receivedParameters.complete(parameters)

            testEventData
        }

        pool.push(event)
    }
}
