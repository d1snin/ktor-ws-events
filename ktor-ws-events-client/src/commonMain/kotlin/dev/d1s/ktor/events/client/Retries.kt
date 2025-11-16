package dev.d1s.ktor.events.client

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.lighthousegames.logging.logging

private const val DEFAULT_DELAY = 5_000L

private val logger = logging()

public suspend fun <R> withRetries(
    continuous: Boolean = false,
    delay: Long = DEFAULT_DELAY,
    onError: suspend (Throwable) -> Unit = {},
    block: suspend () -> R
) {
    coroutineScope {
        while (true) {
            try {
                block()

                if (!continuous) {
                    break
                }
            } catch (e: Throwable) {
                logger.d {
                    "Error while executing; ${e::class.simpleName}: ${e.message}"
                }

                onError(e)

                delay(delay)
            }
        }
    }
}