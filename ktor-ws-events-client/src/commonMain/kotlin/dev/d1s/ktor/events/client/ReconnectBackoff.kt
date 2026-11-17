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

import kotlin.random.Random
import kotlin.time.Duration

/**
 * Exponential reconnect backoff with "equal jitter": the n-th consecutive
 * attempt waits a random time in [base/2, base], base = [initial] * 2^(n-1)
 * capped at [max]. Jitter keeps many sessions of one client (and many
 * clients) from reconnecting in lockstep when the server comes back.
 */
internal class ReconnectBackoff(
    private val initial: Duration,
    private val max: Duration,
    private val random: Random = Random.Default
) {

    init {
        require(initial.isPositive()) { "initial reconnect delay must be positive" }
        require(max >= initial) { "max reconnect delay must not be below the initial one" }
    }

    /** Consecutive attempts since the last [reset]. */
    var attempts: Int = 0
        private set

    /** The delay before the next attempt; counts the attempt. */
    fun next(): Duration {
        attempts++

        // doubling past 2^20 can only exceed max
        val base = initial * (1 shl minOf(attempts - 1, 20)).toDouble()
        val capped = if (base > max) max else base

        return capped / 2 + (capped / 2) * random.nextDouble()
    }

    fun reset() {
        attempts = 0
    }
}
