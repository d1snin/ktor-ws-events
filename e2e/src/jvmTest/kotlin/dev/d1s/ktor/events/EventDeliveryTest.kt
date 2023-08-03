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

package dev.d1s.ktor.events

import dev.d1s.ktor.events.client.receiveWebSocketEvent
import dev.d1s.ktor.events.client.webSocketEvents
import dev.d1s.ktor.events.commons.WebSocketEvent
import dev.d1s.ktor.events.commons.event
import dev.d1s.ktor.events.configuration.eventChannel
import dev.d1s.ktor.events.configuration.runTestServer
import dev.d1s.ktor.events.configuration.webSocketClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lighthousegames.logging.logging
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

                    assertEquals(testServerEventReference, event.reference)
                    assertEquals(testEventData.message, event.data.message)
                }
            }

            delay(1_000)

            sendTestEvent()
        }
    }

    private suspend fun listenForTestEvent(block: (WebSocketEvent<TestEventData>) -> Unit) {
        webSocketClient.webSocketEvents(testClientEventReference, loop = false) {
            val event = receiveWebSocketEvent<TestEventData>()

            block(event)
        }
    }

    private suspend fun sendTestEvent() {
        val event = event(testServerEventReference, testEventData)

        eventChannel.send(event)
    }
}