package im.actor.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MobileConfig {
    private List<String> endpoints = new ArrayList<String>();
    private String hockeyApp;
    private String mixpanel;
    private String mint;

    @JsonProperty("endpoints")
    public List<String> getEndpoints() {
        return endpoints;
    }

    @JsonProperty("endpoints")
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    @JsonProperty("hockeyapp")
    public String getHockeyApp() {
        return hockeyApp;
    }

    @JsonProperty("hockeyapp")
    public void setHockeyApp(String hockeyApp) {
        this.hockeyApp = hockeyApp;
    }

    @JsonProperty("mixpanel")
    public String getMixpanel() {
        return mixpanel;
    }

    @JsonProperty("mixpanel")
    public void setMixpanel(String mixpanel) {
        this.mixpanel = mixpanel;
    }

    @JsonProperty("mint")
    public String getMint() {
        return mint;
    }

    @JsonProperty("mint")
    public void setMint(String mint) {
        this.mint = mint;
    }
}
