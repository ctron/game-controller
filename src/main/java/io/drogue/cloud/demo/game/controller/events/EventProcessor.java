package io.drogue.cloud.demo.game.controller.events;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.drogue.cloud.demo.game.controller.events.ditto.Client;
import io.drogue.cloud.demo.game.controller.events.model.Event;
import io.drogue.cloud.demo.game.controller.events.model.Leds;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    @Inject
    Client ditto;

    @Incoming("sensor-events")
    public Uni<Void> sensorEvent(final Event event) {
        log.info("Event: {}", event);

        final var temperature = getTemperature(event).orElse(null);
        log.info("Temperature: {}", temperature);

        final var leds = new Leds();

        if (temperature != null && temperature > 30) {
            leds.one = true;
        }

        return setLeds(event.getThingId(), leds);
    }

    private Uni<Void> setLeds(final String thingId, final Leds leds) {
        return this.ditto
                .setDesiredProperties(thingId, "leds", leds)
                .replaceWithVoid();
    }

    private Optional<Double> getTemperature(final Event event) {
        return Optional
                .ofNullable(event.getFeatures().get("temperature"))
                .flatMap(temp -> Optional.ofNullable(temp.getProperties().get("value")))
                .filter(Double.class::isInstance)
                .map(Double.class::cast);
    }

}
