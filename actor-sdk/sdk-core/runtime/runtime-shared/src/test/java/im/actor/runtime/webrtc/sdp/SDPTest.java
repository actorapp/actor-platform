package im.actor.runtime.webrtc.sdp;

import org.junit.Test;

import im.actor.runtime.webrtc.sdp.entities.SDPMedia;
import im.actor.runtime.webrtc.sdp.entities.SDPMediaMode;

import static org.junit.Assert.assertEquals;

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

    private String sdp2 = "v=0\n" +
            "o=- 0 2 IN IP4 127.0.0.1\n" +
            "s=-\n" +
            "t=0 0\n" +
            "a=msid-semantic:WMS\n" +
            "a=group:BUNDLE audio video data\n" +
            "m=audio 1 RTP/SAVPF 111 103 0 8 106 105 13 126\n" +
            "a=sendrecv\n" +
            "a=mid:audio\n" +
            "a=rtcp-mux\n" +
            "a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:7S0K6v625mPQw8wPxQHgKFvzaEi1gAGOQ6ieXifj\n" +
            "a=rtpmap:111 opus/48000/2\n" +
            "a=rtpmap:103 ISAC/16000/1\n" +
            "a=rtpmap:0 PCMU/8000/1\n" +
            "a=rtpmap:8 PCMA/8000/1\n" +
            "a=rtpmap:106 CN/32000/1\n" +
            "a=rtpmap:105 CN/16000/1\n" +
            "a=rtpmap:13 CN/8000/1\n" +
            "a=rtpmap:126 telephone-event/8000/1\n" +
            "a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level\n" +
            "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n" +
            "a=ssrc:627951608 cname:localCname\n" +
            "a=ssrc:627951608 msid:4c0246cb-db9a-4aff-a4d8-f2bb2298eecb f1f2b0c5-5c71-4c6e-8650-319f570e4c49\n" +
            "a=ice-ufrag:quqrS4gpAWmlQ3MZ\n" +
            "a=ice-pwd:ls7ebOSxNP5ei9ZdIjfslVDs\n" +
            "m=video 1 RTP/SAVPF 100 96\n" +
            "a=sendrecv\n" +
            "a=mid:video\n" +
            "a=rtcp-mux\n" +
            "a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:7S0K6v625mPQw8wPxQHgKFvzaEi1gAGOQ6ieXifj\n" +
            "a=rtpmap:100 VP8/90000\n" +
            "a=rtcp-fb:100 ccm fir\n" +
            "a=rtcp-fb:100 nack\n" +
            "a=rtcp-fb:100 goog-remb\n" +
            "a=rtpmap:96 rtx/90000\n" +
            "a=fmtp:96 apt=100\n" +
            "a=rtcp-fb:96 ccm fir\n" +
            "a=rtcp-fb:96 nack\n" +
            "a=rtcp-fb:96 goog-remb\n" +
            "a=extmap:2 urn:ietf:params:rtp-hdrext:toffset\n" +
            "a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time\n" +
            "a=ssrc-group:SIM 1498522288 1414599019 130364185\n" +
            "a=ssrc-group:FID 1498522288 1124771813\n" +
            "a=ssrc-group:FID 1414599019 947126490\n" +
            "a=ssrc-group:FID 130364185 1521365890\n" +
            "a=ssrc:1498522288 cname:localCname\n" +
            "a=ssrc:1498522288 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ssrc:1414599019 cname:localCname\n" +
            "a=ssrc:1414599019 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ssrc:130364185 cname:localCname\n" +
            "a=ssrc:130364185 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ssrc:1124771813 cname:localCname\n" +
            "a=ssrc:1124771813 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ssrc:947126490 cname:localCname\n" +
            "a=ssrc:947126490 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ssrc:1521365890 cname:localCname\n" +
            "a=ssrc:1521365890 msid:68aedb58-b7c6-4887-95c6-f048d9de8442 a1504603-878f-4eca-8b3d-0a2b6e9f5dc0\n" +
            "a=ice-ufrag:quqrS4gpAWmlQ3MZ\n" +
            "a=ice-pwd:ls7ebOSxNP5ei9ZdIjfslVDs\n" +
            "m=application 1 RTP/SAVPF 101\n" +
            "a=sendrecv\n" +
            "a=mid:data\n" +
            "a=rtcp-mux\n" +
            "a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:7S0K6v625mPQw8wPxQHgKFvzaEi1gAGOQ6ieXifj\n" +
            "a=rtpmap:101 google-data/90000\n" +
            "a=ssrc:978055802 cname:localCname\n" +
            "a=ssrc:978055802 msid:sendDataChannel sendDataChannel\n" +
            "a=ice-ufrag:quqrS4gpAWmlQ3MZ\n" +
            "a=ice-pwd:ls7ebOSxNP5ei9ZdIjfslVDs\n";

    @Test
    public void testSDPParser() {
        SDPScheme sdpScheme = SDP.parse(sdp);

        // Check serializer
        // String serialized = sdpScheme.toSDP().replace("\r", "");
        // assertEquals(sdp, serialized);

        assertEquals(1, sdpScheme.getMediaLevel().size());
        SDPMedia media = sdpScheme.getMediaLevel().get(0);
        assertEquals("audio", media.getType());
        assertEquals(SDPMediaMode.SEND_RECEIVE, media.getMode());
        // Protocols
        assertEquals("UDP/TLS/RTP/SAVPF", media.getProtocol());
        // Codecs
        assertEquals(9, media.getCodecs().size());
        assertEquals(111, media.getCodecs().get(0).getIndex());
        assertEquals(103, media.getCodecs().get(1).getIndex());
        assertEquals(9, media.getCodecs().get(2).getIndex());
        assertEquals(0, media.getCodecs().get(3).getIndex());
        assertEquals(8, media.getCodecs().get(4).getIndex());
        assertEquals(106, media.getCodecs().get(5).getIndex());
        assertEquals(105, media.getCodecs().get(6).getIndex());
        assertEquals(13, media.getCodecs().get(7).getIndex());
        assertEquals(126, media.getCodecs().get(8).getIndex());
    }

    @Test
    public void testSDP2() {
        SDPScheme sdpScheme = SDP.parse(sdp2);
        String serialized = sdpScheme.toSDP().replace("\r", "");
        // assertEquals(sdp2, serialized);
    }
}
