package dev.d1s.ktor.events.server.pool

import dev.d1s.ktor.events.commons.EventReference
import dev.d1s.ktor.events.commons.Identifier
import dev.d1s.ktor.events.server.entity.ServerWebSocketEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private const val DEFAULT_LIFETIME = 43_200_000L // 12 h
private const val DEFAULT_CLEANUP_INTERVAL = 30_000L // 30 s
private const val DEFAULT_MAX_EVENTS_PER_REFERENCE = 1024

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
    override val lifetimeMillis: Long = DEFAULT_LIFETIME,
    private val cleanupIntervalMillis: Long = DEFAULT_CLEANUP_INTERVAL,
    private val maxEventsPerReference: Int = DEFAULT_MAX_EVENTS_PER_REFERENCE
) : EventPool {

    private val eventMap = ConcurrentHashMap<EventReference, CopyOnWriteArrayList<ServerWebSocketEvent>>()
    private val handlerMap = ConcurrentHashMap<EventReference, MutableList<suspend (ServerWebSocketEvent) -> Unit>>()

    // Confirmations used to scan every event of every reference; this index
    // makes them O(1).
    private val eventsById = ConcurrentHashMap<Identifier, ServerWebSocketEvent>()

    private val gcScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val log = logging()

    init {
        gcScope.launch {
            // This loop used to run with NO delay: it pinned a CPU core at
            // 100% from startup and permanently contended the copy-on-write
            // locks every push() needs, so producers got slower as the pool
            // grew until the process had to be restarted.
            while (true) {
                delay(cleanupIntervalMillis)
                cleanupExpiredEvents()
            }
        }
    }

    override suspend fun push(event: ServerWebSocketEvent) {
        eventMap.compute(event.reference) { _, events ->
            val updatedEvents = events ?: CopyOnWriteArrayList()
            updatedEvents.add(event)

            // Bound the backlog per reference. Trimming to half the cap in one
            // go keeps the O(size) copy-on-write rebuild amortized O(1) per
            // push instead of paying it on every overflowing push.
            if (updatedEvents.size > maxEventsPerReference) {
                val dropCount = updatedEvents.size - maxEventsPerReference / 2
                val dropped = updatedEvents.take(dropCount)

                dropped.forEach { droppedEvent ->
                    eventsById.remove(droppedEvent.id)
                }

                return@compute CopyOnWriteArrayList(updatedEvents.drop(dropCount))
            }

            updatedEvents
        }

        eventsById[event.id] = event

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
        eventsById[event]?.apply {
            if (!acceptedByClients.contains(client)) {
                acceptedByClients += client
            }
        }
    }

    private fun cleanupExpiredEvents() {
        val now = Instant.now().toEpochMilli()

        eventMap.entries.removeIf { (_, events) ->
            events.removeIf { event ->
                val expired = event.initiated + lifetimeMillis < now

                if (expired) {
                    eventsById.remove(event.id)
                }

                expired
            }

            events.isEmpty()
        }
    }
}