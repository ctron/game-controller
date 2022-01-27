package io.drogue.cloud.demo.game.controller.events.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;

public class Event {
    private String thingId;

    private Map<String, Feature> features = new HashMap<>();

    public String getThingId() {
        return thingId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public Map<String, Feature> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Feature> features) {
        this.features = features;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("thingId", thingId)
                .add("features", features)
                .toString();
    }
}
