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

rootProject.name = "ktor-ws-events"

pluginManagement {
    plugins {
        val kotlinVersion = providers.gradleProperty("kotlinVersion").get()

        val dokkaVersion = providers.gradleProperty("dokkaVersion").get()

        val versionsPluginVersion = providers.gradleProperty("versionsPluginVersion").get()

        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion

        id("org.jetbrains.dokka") version dokkaVersion

        id("io.github.ben-manes.versions") version versionsPluginVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    "e2e",
    "ktor-ws-events-client",
    "ktor-ws-events-commons",
    "ktor-ws-events-server"
)