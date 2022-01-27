package io.drogue.cloud.demo.game.controller.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Leds {
    @JsonProperty("1")
    public boolean one;
    @JsonProperty("2")
    public boolean two;
    @JsonProperty("3")
    public boolean three;
    @JsonProperty("4")
    public boolean four;
}
