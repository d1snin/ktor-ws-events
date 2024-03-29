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

apply {
    val publishingScript: String by project

    from(publishingScript)
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_17.majorVersion
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                val kmLogVersion: String by project

                val ktorVersion: String by project

                implementation("org.lighthousegames:logging:$kmLogVersion")

                api(project(":ktor-ws-events-commons"))

                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
                api("io.ktor:ktor-server-websockets:$ktorVersion")
            }
        }
    }
}

