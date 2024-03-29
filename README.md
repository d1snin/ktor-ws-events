[![](https://github.com/d1snin/ktor-ws-events/actions/workflows/e2e-test.yml/badge.svg)](https://github.com/d1snin/ktor-ws-events/actions/workflows/e2e-test.yml)
[![](https://maven.d1s.dev/api/badge/latest/releases/dev/d1s/ktor-ws-events/ktor-ws-events-commons?color=40c14a&name=maven.d1s.dev&prefix=v)](https://maven.d1s.dev/#/releases/dev/d1s/ktor-ws-events)

Event streaming extensions for Ktor over WebSocket protocol.
Support is provided for both client and server.

### Installation

```kotlin
repositories {
    maven(url = "https://maven.d1s.dev/releases")
}

dependencies {
    val ktorWsEventsVersion: String by project

    // server side
    implementation("dev.d1s.ktor-ws-events:ktor-ws-events-server:$ktorWsEventsVersion")

    // client side
    implementation("dev.d1s.ktor-ws-events:ktor-ws-events-client:$ktorWsEventsVersion")
}
```

### Example usage on the server side

```kotlin
val eventChannel = WebSocketEventChannel()

fun Application.configureWebSocketEvents() {
    install(WebSocketEvents) {
        channel = eventChannel
    }

    routing {
        webSocketEvents()
    }
}

fun handleServerFailure(failure: ServerFailure) {
    val reference = ref("server_failure")
    val event = event(reference) {
        failure
    }

    eventChannel.send(event)
}
```

### Example usage on the client side

```kotlin
fun HttClient.configureWebSocketEvents() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    
    install(WebSocketEvents) {
        host = "example.com"
        port = 9090
    }
}

fun HttpClient.listenToServerFailures() {
    val reference = ref("server_failure")

    webSocketEvents(reference) {
        val event = receiveWebSocketEvent<ServerFailure>()
        val failure = event.data

        notifyTeam(failure)
    }
}
```

### How to contribute

See [CONTRIBUTING.md][contribution_guide]

### Code of Conduct

See [CODE_OF_CONDUCT.md][code_of_conduct]

### License

```text
   Copyright 2022-2024 Mikhail Titov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

[contribution_guide]: https://github.com/d1snin/ktor-ws-events/blob/main/CONTRIBUTING.md
[code_of_conduct]: https://github.com/d1snin/ktor-ws-events/blob/main/CODE_OF_CONDUCT.md
