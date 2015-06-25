package im.actor.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class WebConfig {

    private List<String> endpoints = new ArrayList<String>();

    @JsonProperty("enpoints")
    public List<String> getEndpoints() {
        return endpoints;
    }

    @JsonProperty("enpoints")
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}
