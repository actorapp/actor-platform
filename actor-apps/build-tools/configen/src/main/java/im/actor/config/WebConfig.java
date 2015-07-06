package im.actor.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class WebConfig {

    private List<String> endpoints = new ArrayList<String>();
    private String baseVersion;
    private String mixpanel;

    private boolean isCommunityEnabled;

    @JsonProperty("mixpanel")
    public String getMixpanel() {
        return mixpanel;
    }

    @JsonProperty("mixpanel")
    public void setMixpanel(String mixpanel) {
        this.mixpanel = mixpanel;
    }

    @JsonProperty("community")
    public boolean isCommunityEnabled() {
        return isCommunityEnabled;
    }

    @JsonProperty("community")
    public void setIsCommunityEnabled(boolean isCommunityEnabled) {
        this.isCommunityEnabled = isCommunityEnabled;
    }

    @JsonProperty("base_version")
    public String getBaseVersion() {
        return baseVersion;
    }

    @JsonProperty("base_version")
    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    @JsonProperty("enpoints")
    public List<String> getEndpoints() {
        return endpoints;
    }

    @JsonProperty("enpoints")
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}
