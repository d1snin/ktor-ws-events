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

package dev.d1s.ktor.events.server.util

import dev.d1s.ktor.events.server.EventProcessor
import dev.d1s.ktor.events.server.OutgoingEventFilter
import dev.d1s.ktor.events.server.WEBSOCKET_EVENTS_PLUGIN_NAME
import dev.d1s.ktor.events.server.pool.EventPool
import io.ktor.util.*

internal var Attributes.eventProcessor: EventProcessor
    get() = this[Key.EventProcessor]
    set(value) = this.put(Key.EventProcessor, value)

internal var Attributes.eventPool: EventPool
    get() = this[Key.EventPool]
    set(value) = this.put(Key.EventPool, value)

internal var Attributes.filter: OutgoingEventFilter?
    get() = this.getOrNull(Key.Filter)
    set(value) {
        value?.let {
            this.put(Key.Filter, it)
        }
    }

private object Key {

    val EventProcessor =
        AttributeKey<EventProcessor>("${WEBSOCKET_EVENTS_PLUGIN_NAME}_event-processor")

    val EventPool = AttributeKey<EventPool>("${WEBSOCKET_EVENTS_PLUGIN_NAME}_event-pool")

    val Filter = AttributeKey<OutgoingEventFilter>("${WEBSOCKET_EVENTS_PLUGIN_NAME}_filter")
}