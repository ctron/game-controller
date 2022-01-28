package io.drogue.cloud.demo.game.controller.events.ditto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.drogue.cloud.demo.game.controller.events.model.Leds;
import io.vertx.core.json.JsonObject;

public class ClientTest {

    @Test
    public void testDeepExpandLeds() {

        final var leds = new Leds();
        leds.two = true;

        final var actual = Client.buildCondition("features/leds/desiredProperties", JsonObject.mapFrom(leds));

        Assertions.assertEquals("or(ne(features/leds/desiredProperties/1,false),ne(features/leds/desiredProperties/2,true),ne(features/leds/desiredProperties/3,false),ne(features/leds/desiredProperties/4,false))", actual);
    }

    @Test
    public void testDeepExpand() {

        final var json = new JsonObject()
                .put("foo", "bar")
                .put("sub", new JsonObject()
                        .put("c1", true)
                        .put("c2", 2)
                        .put("c3", 2.3)
                        .put("c4", "some\"foo,bar")
                );

        final var actual = Client.buildCondition("features/f1/properties", json);

        Assertions.assertEquals("or(ne(features/f1/properties/foo,\"bar\"),ne(features/f1/properties/sub/c1,true),ne(features/f1/properties/sub/c2,2),ne(features/f1/properties/sub/c3,2.3),ne(features/f1/properties/sub/c4,\"some%22foo%2Cbar\"))", actual);
    }

}
