package io.drogue.cloud.demo.game.controller.events.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class Leds {
    @JsonProperty("1")
    public boolean one;
    @JsonProperty("2")
    public boolean two;
    @JsonProperty("3")
    public boolean three;
    @JsonProperty("4")
    public boolean four;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Leds leds = (Leds) o;
        return this.one == leds.one && this.two == leds.two && this.three == leds.three && this.four == leds.four;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.one, this.two, this.three, this.four);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("one", this.one)
                .add("two", this.two)
                .add("three", this.three)
                .add("four", this.four)
                .toString();
    }
}
