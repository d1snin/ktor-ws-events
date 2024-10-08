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

package dev.d1s.ktor.events.configuration

import dev.d1s.ktor.events.client.WebSocketEvents
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*

val webSocketClient = HttpClient(CIO) {
    install(WebSockets) {
        contentConverter = JacksonWebsocketContentConverter()
    }

    install(WebSocketEvents) {
        url = "ws://localhost:$TEST_SERVER_PORT"
        clientId = "test-client"
    }
}
