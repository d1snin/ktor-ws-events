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

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("multiplatform") apply false
    id("org.jetbrains.dokka")
    id("io.github.ben-manes.versions")
}

val javaToolchainVersion = 25
val jvmBytecodeTarget = JvmTarget.JVM_17

val dokkaFooterMessage = "Copyright (c) 2022-2023 Mikhail Titov"
val dokkaExcludedModules = listOf("e2e")

allprojects {
    group = providers.gradleProperty("projectGroup").get()
    version = providers.gradleProperty("projectVersion").get()

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.multiplatform")
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            events.addAll(
                listOf(
                    TestLogEvent.PASSED,
                    TestLogEvent.FAILED,
                    TestLogEvent.SKIPPED,
                    TestLogEvent.STANDARD_OUT,
                    TestLogEvent.STANDARD_ERROR,
                )
            )
        }
    }

    // Built with JDK 25, but the published bytecode and the JDK API surface stay at 17.
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(jvmBytecodeTarget)
            freeCompilerArgs.add("-Xjdk-release=${jvmBytecodeTarget.target}")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(jvmBytecodeTarget.target.toInt())
    }

    if (project.name !in dokkaExcludedModules) {
        apply {
            plugin("org.jetbrains.dokka")
        }

        configure<DokkaExtension> {
            dokkaSourceSets.configureEach {
                includes.from(providers.gradleProperty("moduleDocsPath"))
            }

            pluginsConfiguration.html {
                footerMessage.set(dokkaFooterMessage)
            }
        }
    }

    configure<KotlinMultiplatformExtension> {
        explicitApi()

        jvmToolchain(javaToolchainVersion)
    }
}

dependencies {
    subprojects
        .filter { it.name !in dokkaExcludedModules }
        .forEach { dokka(project(it.path)) }
}

dokka {
    dokkaPublications.html {
        includes.from("README.md")
    }

    pluginsConfiguration.html {
        footerMessage.set(dokkaFooterMessage)
    }
}
