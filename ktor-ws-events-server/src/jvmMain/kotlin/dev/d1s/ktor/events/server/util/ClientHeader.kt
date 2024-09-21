package dev.d1s.ktor.events.server.util

import dev.d1s.ktor.events.commons.util.Routes
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*

internal val ApplicationCall.clientId
    get() = request.header(Routes.CLIENT_ID_HEADER)
        ?: throw BadRequestException("${Routes.CLIENT_ID_HEADER} not specified")