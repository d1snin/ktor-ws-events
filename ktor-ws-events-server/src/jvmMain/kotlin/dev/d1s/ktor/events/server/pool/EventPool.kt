package dev.d1s.ktor.events.server.pool

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.server.entity.ServerWebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private const val DEFAULT_LIFETIME = 43_200_000L // 12 h

public interface EventPool {

    public val lifetimeMillis: Long

    public suspend fun push(event: ServerWebSocketEvent)

    public fun get(reference: EventReference, client: Identifier): List<ServerWebSocketEvent>

    public fun onEvent(
        reference: EventReference,
        client: Identifier,
        handler: suspend (ServerWebSocketEvent) -> Unit
    )

    public fun confirm(event: Identifier, client: Identifier)
}

public class InMemoryEventPool(
    override val lifetimeMillis: Long = DEFAULT_LIFETIME
) : EventPool {

    private val eventMap = ConcurrentHashMap<EventReference, CopyOnWriteArrayList<ServerWebSocketEvent>>()
    private val handlerMap = ConcurrentHashMap<EventReference, MutableList<suspend (ServerWebSocketEvent) -> Unit>>()

    private val gcScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val log = logging()

    init {
        gcScope.launch {
            while (true) {
                cleanupExpiredEvents()
            }
        }
    }

    override suspend fun push(event: ServerWebSocketEvent) {
        eventMap.compute(event.reference) { _, events ->
            val updatedEvents = events ?: CopyOnWriteArrayList()
            updatedEvents.add(event)
            updatedEvents
        }

        log.d {
            "Pushing event $event;\ncurrent event map: $eventMap;\nhandler map: $handlerMap"
        }

        val handlersForReference = handlerMap.entries.find {
            if (it.key.group != event.reference.group) return@find false

            val otherPrincipal = event.reference.principal

            if (it.key.principal != null) {
                return@find if (otherPrincipal != null) {
                    it.key.principal == otherPrincipal
                } else {
                    false
                }
            }

            true
        }?.value

        log.d {
            "Got handlers for reference: $handlersForReference"
        }

        if (handlersForReference != null) {
            coroutineScope {
                handlersForReference.forEach { handler ->
                    launch {
                        handler(event)
                    }
                }
            }
        }
    }

    override fun get(reference: EventReference, client: Identifier): List<ServerWebSocketEvent> =
        eventMap[reference]?.filter { event ->
            !event.acceptedByClients.contains(client)
        } ?: emptyList()

    override fun onEvent(
        reference: EventReference,
        client: Identifier,
        handler: suspend (ServerWebSocketEvent) -> Unit
    ) {
        handlerMap.compute(reference) { _, handlers ->
            val updatedHandlers = handlers ?: mutableListOf()
            updatedHandlers.add(handler)
            updatedHandlers
        }
    }

    override fun confirm(event: Identifier, client: Identifier) {
        for (events in eventMap.values) {
            events.find { it.id == event }?.apply {
                if (!acceptedByClients.contains(client)) {
                    acceptedByClients += client
                }
            }
        }
    }

    private fun cleanupExpiredEvents() {
        val now = Instant.now().toEpochMilli()

        eventMap.entries.removeIf { (_, events) ->
            events.removeIf { event ->
                event.initiated + lifetimeMillis < now
            }

            events.isEmpty()
        }
    }
}