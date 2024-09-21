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
import dev.d1s.ktor.events.configuration.pool
import dev.d1s.ktor.events.configuration.runTestServer
import dev.d1s.ktor.events.configuration.webSocketClient
import dev.d1s.ktor.events.server.entity.event
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lighthousegames.logging.logging
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EventDeliveryTest {

    private val log = logging()

    @BeforeTest
    fun prepareServer() {
        runTestServer()
    }

    @Test
    fun `server must deliver an event`() {
        runBlocking {
            launch {
                listenForTestEvent { event ->
                    log.i {
                        "Received test event: $event"
                    }

                    assertEquals(event.reference, testServerEventReference)
                    assertEquals(testEventData.message, event.data.message)
                }
            }

            delay(1_000)

            sendTestEvent()
        }
    }

    private suspend fun listenForTestEvent(block: (ClientWebSocketEvent<TestEventData>) -> Unit) {
        webSocketClient.webSocketEvents(testClientEventReference) {
            val event = receiveWebSocketEvent<TestEventData>()

            block(event)
        }
    }

    private suspend fun sendTestEvent() {
        val event = event(testServerEventReference) { parameters ->
            log.i {
                "Got parameters: $parameters"
            }

            val testParameterData = parameters[TEST_CLIENT_PARAMETER_KEY]

            assertNotNull(testParameterData)
            assertEquals(TEST_CLIENT_PARAMETER_DATA, testParameterData)

            testEventData
        }

        pool.push(event)
    }
}