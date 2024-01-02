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

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_17.majorVersion
        }
    }

    sourceSets {
        val jvmTest by getting {
            dependencies {
                val ktorVersion: String by project

                val kmLogVersion: String by project
                val logbackVersion: String by project

                implementation("org.lighthousegames:logging:$kmLogVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")

                implementation(project(":ktor-ws-events-server"))
                implementation(project(":ktor-ws-events-client"))

                implementation(kotlin("test-junit"))

                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
                implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
            }
        }
    }
}


