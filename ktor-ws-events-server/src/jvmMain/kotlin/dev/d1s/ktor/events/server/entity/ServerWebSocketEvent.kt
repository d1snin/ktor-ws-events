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

import dev.d1s.ktor.events.commons.*
import java.time.Instant

public typealias EventDataSupplier = suspend (ClientParameters) -> Any

/**
 * Example usage:
 * ```kotlin
 * val reference = ref("book_created")
 * val createdBook: Book = createBook()
 *
 * val event = event(reference) { _ ->
 *     createdBook
 * }
 * ```
 *
 * @see event
 * @see dev.d1s.ktor.events.commons.ref
 */
public data class ServerWebSocketEvent(
    override val id: Identifier = randomId,
    override val reference: EventReference,
    override val initiated: UnixTime = Instant.now().toEpochMilli(),
    internal var acceptedByClients: List<Identifier> = listOf(),
    internal val dataSupplier: EventDataSupplier
) : AbstractEvent

/**
 * A shortcut. Returns `ServerWebSocketEvent(reference, data)` currently initiated.
 *
 * @see ServerWebSocketEvent
 */
public fun event(reference: EventReference, dataSupplier: EventDataSupplier): ServerWebSocketEvent =
    ServerWebSocketEvent(reference = reference, dataSupplier = dataSupplier)
