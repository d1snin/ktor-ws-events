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

apply(from = rootProject.file("publishing.gradle.kts"))

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                val kmLogVersion = providers.gradleProperty("kmLogVersion").get()

                val ktorVersion = providers.gradleProperty("ktorVersion").get()

                implementation("com.diamondedge:logging:$kmLogVersion")

                api(project(":ktor-ws-events-commons"))

                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
                api("io.ktor:ktor-server-websockets:$ktorVersion")
            }
        }
    }
}

