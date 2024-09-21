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

package dev.d1s.ktor.events.commons

import kotlinx.serialization.Serializable

/**
 * Event group is used to combine events of the same type.
 * For example: "book_updated" or "book_created".
 *
 * @see EventPrincipal
 * @see EventReference
 */
public typealias EventGroup = String

/**
 * Event principal is used to combine events related to the same object.
 * It could be any identifier of the object.
 *
 * For example:
 * ```kotlin
 * val reference = ref(group = "book_updated", principal = "528975892357")
 * ```
 * Where `principal` is a book ID.
 *
 * @see EventGroup
 * @see EventReference
 * @see ref
 */
public typealias EventPrincipal = String?

/**
 * Client parameters passed to the server.
 * The parameters are only scoped to the connection.
 * When firing an event, server can handle them.
 */
public typealias ClientParameters = Map<String, String>

/**
 * Event reference acts as a type for all events.
 *
 * Example usage:
 * ```kotlin
 * val reference = ref(group = "book_updated", principal = "528975892357")
 * ```
 *
 * @see EventGroup
 * @see EventPrincipal
 * @see ClientParameters
 * @see ref
 */
@Serializable
public data class EventReference(
    val group: EventGroup,
    val principal: EventPrincipal = null,
    val parameters: ClientParameters = mapOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this::class != other?.let { it::class }) return false

        other as EventReference

        return group == other.group
    }

    override fun hashCode(): Int =
        group.hashCode()
}

/**
 * A shortcut.
 *
 * @see EventReference
 * @see ClientParameters
 */
public fun ref(
    group: EventGroup,
    principal: EventPrincipal = null,
    clientParameters: ClientParameters = mapOf()
): EventReference = EventReference(group = group, principal = principal, parameters = clientParameters)