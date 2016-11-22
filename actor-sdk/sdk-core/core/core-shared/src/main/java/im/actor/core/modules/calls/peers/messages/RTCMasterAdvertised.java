package im.actor.core.modules.calls.peers.messages;

import java.util.ArrayList;

import im.actor.core.api.ApiICEServer;

public class RTCMasterAdvertised {

    private ArrayList<ApiICEServer> iceServers;

    public RTCMasterAdvertised(ArrayList<ApiICEServer> iceServers) {
        this.iceServers = iceServers;
    }

    public ArrayList<ApiICEServer> getIceServers() {
        return iceServers;
    }
}
