package im.actor.model;

/**
 * Created by ex3ndr on 07.03.15.
 */
public class ApiConfiguration {

    private final String appTitle;

    private final int appId;
    private final String appKey;

    private final String deviceTitle;
    private final byte[] deviceHash;

    public ApiConfiguration(String appTitle, int appId, String appKey, String deviceTitle, byte[] deviceHash) {
        this.appTitle = appTitle;
        this.appId = appId;
        this.appKey = appKey;
        this.deviceTitle = deviceTitle;
        this.deviceHash = deviceHash;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public int getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getDeviceTitle() {
        return deviceTitle;
    }

    public byte[] getDeviceHash() {
        return deviceHash;
    }
}
