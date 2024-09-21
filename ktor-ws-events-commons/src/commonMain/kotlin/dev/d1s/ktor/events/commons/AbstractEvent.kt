package dev.d1s.ktor.events.commons

public interface AbstractEvent {

    public val id: Identifier

    public val reference: EventReference

    public val initiated: UnixTime
}