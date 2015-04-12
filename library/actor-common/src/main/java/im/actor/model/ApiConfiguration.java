package im.actor.model;

/**
 * API Configuration for accessing Actor Platform servers
 */
public class ApiConfiguration {

    private final String appTitle;

    private final int appId;
    private final String appKey;

    private final String deviceTitle;
    private final String deviceString;

    /**
     * Construct API Configuration
     *
     * @param appTitle     title of application
     * @param appId        app id for API
     * @param appKey       app key for API
     * @param deviceTitle  device title
     * @param deviceString device unique key
     */
    public ApiConfiguration(String appTitle, int appId, String appKey, String deviceTitle, String deviceString) {
        this.appTitle = appTitle;
        this.appId = appId;
        this.appKey = appKey;
        this.deviceTitle = deviceTitle;
        this.deviceString = deviceString;
    }

    /**
     * Get App Title
     *
     * @return the App Title
     */
    public String getAppTitle() {
        return appTitle;
    }

    /**
     * Get App API Id
     *
     * @return the App Id
     */
    public int getAppId() {
        return appId;
    }

    /**
     * Get App API Key
     *
     * @return the App Key
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * Get Device Title
     *
     * @return the Device Title
     */
    public String getDeviceTitle() {
        return deviceTitle;
    }

    /**
     * Get Device unique string
     *
     * @return the Unique String
     */
    public String getDeviceString() {
        return deviceString;
    }
}
