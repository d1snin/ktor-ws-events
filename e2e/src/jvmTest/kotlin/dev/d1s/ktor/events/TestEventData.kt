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

package dev.d1s.ktor.events

import dev.d1s.ktor.events.commons.ref

const val TEST_MESSAGE = "Hello, World!"

const val TEST_CLIENT_PARAMETER_KEY = "test"
const val TEST_CLIENT_PARAMETER_DATA = "test"

data class TestEventData(
    val message: String
)

val testEventData = TestEventData(TEST_MESSAGE)

val testServerEventReference = ref(
    group = "test_group",
    principal = "test_principal"
)

val testClientEventReference = ref(
    group = "test_group",
    principal = null,
    clientParameters = mapOf(TEST_CLIENT_PARAMETER_KEY to TEST_CLIENT_PARAMETER_DATA)
)