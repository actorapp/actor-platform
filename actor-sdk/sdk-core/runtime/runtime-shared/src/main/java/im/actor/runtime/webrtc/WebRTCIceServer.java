package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * WebRTC Ice Server
 */
public class WebRTCIceServer {

    @NotNull
    @Property("nonatomic, readonly")
    private String url;
    @Nullable
    @Property("nonatomic, readonly")
    private String username;
    @Nullable
    @Property("nonatomic, readonly")
    private String credential;

    /**
     * Default Constructor for ICE server
     *
     * @param url        url for server
     * @param username   optional username for server
     * @param credential optional credential for server
     */
    @ObjectiveCName("initWithUrl:withUserName:withCredential:")
    public WebRTCIceServer(@NotNull String url, @Nullable String username, @Nullable String credential) {
        this.url = url;
        this.username = username;
        this.credential = credential;
    }

    /**
     * Constructor for non-authenticated ICE server
     *
     * @param url url for server
     */
    @ObjectiveCName("initWithUrl:")
    public WebRTCIceServer(@NotNull String url) {
        this(url, null, null);
    }

    /**
     * Get URL to server
     *
     * @return url
     */
    @NotNull
    public String getUrl() {
        return url;
    }

    /**
     * Get Optional username for server
     *
     * @return username
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * Get Optional credential for server
     *
     * @return credential
     */
    @Nullable
    public String getCredential() {
        return credential;
    }
}