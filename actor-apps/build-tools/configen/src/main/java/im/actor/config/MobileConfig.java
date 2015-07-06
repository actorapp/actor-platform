package im.actor.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MobileConfig {
    private List<String> endpoints = new ArrayList<String>();
    private String hockeyApp;
    private String mixpanel;
    private String mint;
    private String baseVersion;
    private Integer pushId;
    private boolean isCommunityEnabled;

    @JsonProperty("push_id")
    public Integer getPushId() {
        return pushId;
    }

    @JsonProperty("push_id")
    public void setPushId(Integer pushId) {
        this.pushId = pushId;
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
