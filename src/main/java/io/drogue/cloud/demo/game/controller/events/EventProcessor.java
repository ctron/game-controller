package io.drogue.cloud.demo.game.controller.events;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.drogue.cloud.demo.game.controller.events.ditto.Client;
import io.drogue.cloud.demo.game.controller.events.model.Event;
import io.drogue.cloud.demo.game.controller.events.model.Leds;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    @Inject
    Client ditto;

    @Incoming("sensor-events")
    public Uni<Void> sensorEvent(final Event event) {
        log.info("Event: {}", event);

        final var leds = new Leds();

        final var temperature = getTemperature(event).orElse(null);
        log.info("Temperature: {}", temperature);
        if (temperature != null && temperature > 30) {
            leds.one = true;
        }

        final var light = getLight(event).orElse(null);
        log.info("Light: {}", temperature);
        if (light != null && light > 2048) {
            leds.two = true;
        }

        final var currentLeds = getDesiredLeds(event).orElse(new Leds());

        if (!currentLeds.equals(leds)) {
            return setLeds(event.getThingId(), leds);
        } else {
            return Uni.createFrom().voidItem();
        }
    }

    private Uni<Void> setLeds(final String thingId, final Leds leds) {
        log.info("Setting LEDs - thing: {}, state: {}", thingId, leds);
        return this.ditto
                .setDesiredProperties(thingId, "leds", leds)
                .replaceWithVoid();
    }

    private Optional<Double> getTemperature(final Event event) {
        return getNumericValue(event, "temperature", Number::doubleValue);
    }

    private Optional<Integer> getLight(final Event event) {
        return getNumericValue(event, "light", Number::intValue);
    }

    private static <T> Optional<T> getNumericValue(final Event event, final String feature, final Function<Number, T> converter) {
        return Optional
                .ofNullable(event.getFeatures().get(feature))
                .flatMap(temp -> Optional.ofNullable(temp.getProperties().get("value")))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(converter);
    }

    private Optional<Leds> getDesiredLeds(final Event event) {
        return Optional
                .ofNullable(event.getFeatures().get("leds"))
                .flatMap(value -> Optional.ofNullable(value.getDesiredProperties()))
                .map(value -> {
                    try {
                        return new JsonObject(value).mapTo(Leds.class);
                    } catch (final Exception e) {
                        return null;
                    }
                });
    }

}
