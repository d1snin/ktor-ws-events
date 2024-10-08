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

package dev.d1s.ktor.events.server.entity

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.server.util.clientId
import io.ktor.server.application.*
import io.ktor.server.websocket.*

public data class EventSendingConnection(
    val reference: EventReference,
    val session: DefaultWebSocketServerSession,
    val call: ApplicationCall = session.call,
    val clientId: Identifier = call.clientId
)
