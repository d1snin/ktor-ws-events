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

package dev.d1s.ktor.events.configuration

import dev.d1s.ktor.events.server.WebSocketEventChannel
import dev.d1s.ktor.events.server.WebSocketEvents
import dev.d1s.ktor.events.server.webSocketEvents
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

const val TEST_SERVER_PORT = 9639

val eventChannel = WebSocketEventChannel()

fun runTestServer() = embeddedServer(Netty, environment).start()

private val environment = applicationEngineEnvironment {
    module {
        install(WebSocketEvents) {
            channel = eventChannel
        }

        routing {
            webSocketEvents()
        }
    }

    connector {
        port = TEST_SERVER_PORT
    }
}
