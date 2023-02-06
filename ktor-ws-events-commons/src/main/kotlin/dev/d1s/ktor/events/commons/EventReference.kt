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
 * Event reference acts as an identifier for a [WebSocketEvent].
 *
 * Example usage:
 * ```kotlin
 * val reference = ref(group = "book_updated", principal = "528975892357")
 * ```
 *
 * @see EventGroup
 * @see EventPrincipal
 * @see ref
 */
public data class EventReference(
    val group: EventGroup,
    val principal: EventPrincipal = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventReference

        if (group != other.group) return false

        val otherPrincipal = other.principal

        if (principal != null) {
            return if (otherPrincipal != null) {
                principal == otherPrincipal
            } else {
                false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + (principal?.hashCode() ?: 0)
        return result
    }
}

/**
 * A shortcut. Returns `EventReference(group, principal)`
 *
 * @see EventReference
 */
public fun ref(group: EventGroup, principal: EventPrincipal = null): EventReference = EventReference(group, principal)