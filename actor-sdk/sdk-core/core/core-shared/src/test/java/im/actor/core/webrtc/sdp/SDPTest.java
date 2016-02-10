package im.actor.core.webrtc.sdp;

import org.junit.Test;

public class SDPTest {

    private String sdp = "v=0\n" +
            "o=- 3369226760567452353 2 IN IP4 127.0.0.1\n" +
            "s=-\n" +
            "t=0 0\n" +
            "a=group:BUNDLE audio\n" +
            "a=msid-semantic: WMS 4l5CG6RIEL30oDxuCDaOXlJSQJDjjEFcPqSZ\n" +
            "m=audio 9 UDP/TLS/RTP/SAVPF 111 103 9 0 8 106 105 13 126\n" +
            "c=IN IP4 0.0.0.0\n" +
            "a=rtcp:9 IN IP4 0.0.0.0\n" +
            "a=ice-ufrag:Cf21p3V3DLMewJzW\n" +
            "a=ice-pwd:GZVX4lWnwLpUF9bgdFP8Rjjm\n" +
            "a=fingerprint:sha-256 63:DF:AC:5B:AC:01:B2:16:33:92:61:4A:83:CE:A1:27:DC:43:12:DF:9D:D2:2B:86:84:58:AB:C9:01:DA:C3:2A\n" +
            "a=setup:active\n" +
            "a=mid:audio\n" +
            "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n" +
            "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n" +
            "a=sendrecv\n" +
            "a=rtcp-mux\n" +
            "a=rtpmap:111 opus/48000/2\n" +
            "a=fmtp:111 minptime=10; useinbandfec=1\n" +
            "a=rtpmap:103 ISAC/16000\n" +
            "a=rtpmap:9 G722/8000\n" +
            "a=rtpmap:0 PCMU/8000\n" +
            "a=rtpmap:8 PCMA/8000\n" +
            "a=rtpmap:106 CN/32000\n" +
            "a=rtpmap:105 CN/16000\n" +
            "a=rtpmap:13 CN/8000\n" +
            "a=rtpmap:126 telephone-event/8000\n" +
            "a=maxptime:60\n" +
            "a=ssrc:419421375 cname:us7FwY4nl+TCdXVa\n" +
            "a=ssrc:419421375 msid:4l5CG6RIEL30oDxuCDaOXlJSQJDjjEFcPqSZ 3b9e5bd4-81bf-4080-b914-2c55696e8ba0\n" +
            "a=ssrc:419421375 mslabel:4l5CG6RIEL30oDxuCDaOXlJSQJDjjEFcPqSZ\n" +
            "a=ssrc:419421375 label:3b9e5bd4-81bf-4080-b914-2c55696e8ba0\n";

    @Test
    public void testSDPParser() {
        SDP.parse(sdp);
    }
}
