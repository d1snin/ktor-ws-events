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

package dev.d1s.ktor.events.commons

import kotlinx.serialization.Serializable

/**
 * Example usage:
 * ```kotlin
 * val reference = ref("book_created")
 * val createdBook: Book = createBook()
 *
 * val event = event(reference, createdBook)
 * ```
 *
 * @param data Any data associated with this [WebSocketEvent].
 * @see event
 * @see ref
 */
@Serializable
public data class WebSocketEvent<T>(
    val reference: EventReference,
    val data: T
)

/**
 * A shortcut. Returns `WebSocketEvent(reference, data)`
 *
 * @see WebSocketEvent
 */
public fun <T> event(reference: EventReference, data: T): WebSocketEvent<T> = WebSocketEvent(reference, data)
