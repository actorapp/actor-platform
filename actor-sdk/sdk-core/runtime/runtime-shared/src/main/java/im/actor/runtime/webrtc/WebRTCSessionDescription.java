/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.webrtc;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import org.jetbrains.annotations.NotNull;

import im.actor.runtime.annotations.Stable;
import im.actor.runtime.annotations.Verified;

/**
 * WebRTC Session description
 */
@Stable
public class WebRTCSessionDescription {

    @NotNull
    @Property("nonatomic, readonly")
    private final String type;
    @NotNull
    @Property("nonatomic, readonly")
    private final String sdp;

    /**
     * Default Constructor for session description
     *
     * @param type type of description. Usually "answer" or "offer".
     * @param sdp  SDP value
     */
    @ObjectiveCName("initWithType:withSDP:")
    public WebRTCSessionDescription(@NotNull String type, @NotNull String sdp) {
        this.type = type;
        this.sdp = sdp;
    }

    /**
     * Get Description type
     *
     * @return description type
     */
    @NotNull
    public String getType() {
        return type;
    }

    /**
     * Get SDP value
     *
     * @return SDP value
     */
    @NotNull
    public String getSdp() {
        return sdp;
    }
}
