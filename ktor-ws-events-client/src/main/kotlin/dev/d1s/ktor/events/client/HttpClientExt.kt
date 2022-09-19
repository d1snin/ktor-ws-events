/*
 * Copyright 2022 Mikhail Titov
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

import dev.d1s.ktor.events.commons.constant.DEFAULT_EVENTS_ROUTE
import dev.d1s.ktor.events.commons.constant.GROUP_SEGMENT_PLACEHOLDER
import dev.d1s.ktor.events.commons.constant.PRINCIPAL_QUERY_PARAMETER
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*

public suspend fun HttpClient.wsEvents(
    host: String,
    port: Int,
    eventGroup: String,
    eventPrincipal: String? = null,
    path: String = DEFAULT_EVENTS_ROUTE.replace(GROUP_SEGMENT_PLACEHOLDER, eventGroup),
    block: suspend DefaultWebSocketSession.() -> Unit

) {
    webSocket(
        host = host,
        port = port,
        path = path,
        request = { parameter(PRINCIPAL_QUERY_PARAMETER, eventPrincipal) }) {
        block()
    }
}