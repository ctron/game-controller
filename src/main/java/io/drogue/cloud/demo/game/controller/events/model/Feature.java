package io.drogue.cloud.demo.game.controller.events.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;

public class Feature {
    private Map<String, Object> properties = new HashMap<>();

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("properties", properties)
                .toString();
    }
}
