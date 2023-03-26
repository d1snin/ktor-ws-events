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

package dev.d1s.ktor.events.commons.util

public object Routes {

    public const val GROUP_PATH_PARAMETER: String = "group"
    public const val GROUP_SEGMENT_PLACEHOLDER: String = "{${GROUP_PATH_PARAMETER}}"

    public const val DEFAULT_EVENTS_ROUTE: String = "/events/$GROUP_SEGMENT_PLACEHOLDER"

    public const val PRINCIPAL_QUERY_PARAMETER: String = "principal"
}