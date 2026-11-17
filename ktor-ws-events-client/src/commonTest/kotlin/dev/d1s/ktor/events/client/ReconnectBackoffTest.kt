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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ReconnectBackoffTest {

    @Test
    fun delaysDoubleWithinJitterBoundsUpToTheCap() {
        val backoff = ReconnectBackoff(500.milliseconds, 30.seconds, Random(42))
        val bases = listOf(500, 1_000, 2_000, 4_000, 8_000, 16_000, 30_000, 30_000, 30_000).map { it.milliseconds }

        bases.forEachIndexed { index, base ->
            val delay = backoff.next()

            assertTrue(delay >= base / 2 && delay <= base, "attempt ${index + 1}: $delay not in [${base / 2}, $base]")
            assertEquals(index + 1, backoff.attempts)
        }
    }

    @Test
    fun manyAttemptsNeverOverflowPastTheCap() {
        val backoff = ReconnectBackoff(500.milliseconds, 30.seconds)

        repeat(1_000) {
            assertTrue(backoff.next() <= 30.seconds)
        }
    }

    @Test
    fun resetStartsOver() {
        val backoff = ReconnectBackoff(500.milliseconds, 30.seconds)

        repeat(10) { backoff.next() }
        backoff.reset()

        assertEquals(0, backoff.attempts)
        assertTrue(backoff.next() <= 500.milliseconds)
    }

    @Test
    fun rejectsInvalidBounds() {
        assertFailsWith<IllegalArgumentException> { ReconnectBackoff(0.milliseconds, 1.seconds) }
        assertFailsWith<IllegalArgumentException> { ReconnectBackoff(2.seconds, 1.seconds) }
    }
}
