package io.drogue.cloud.demo.game.controller.events.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class EventDeserializer extends ObjectMapperDeserializer<Event> {
    public EventDeserializer() {
        super(Event.class);
    }
}
