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

import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.lighthousegames.logging.KmLog
import org.lighthousegames.logging.logging

public val EventReceivingScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

public val EventReceiverLog: KmLog = logging()

/**
 * Dequeues a frame containing [ClientWebSocketEvent] and tries to deserialize it.
 *
 * @see webSocketEvents
 */
public suspend inline fun <reified T> DefaultClientWebSocketSession.receiveWebSocketEvent(): ClientWebSocketEvent<T> =
    receiveDeserialized<ClientWebSocketEvent<T>>()

/**
 * Dequeues frames containing [ClientWebSocketEvent] and tries to deserialize it. Will retry if something went wrong while receiving a frame.
 *
 * @see webSocketEvents
 */
public suspend inline fun <reified T> DefaultClientWebSocketSession.receiveWebSocketEvents(crossinline receiver: suspend (ClientWebSocketEvent<T>) -> Unit) {
    withRetries(continuous = true, onError = {
        EventReceiverLog.w {
            "Error receiving web socket events: ${it.message}"

            it.printStackTrace()
        }
    }) {
        val event = receiveWebSocketEvent<T>()
        receiver(event)
    }
}
