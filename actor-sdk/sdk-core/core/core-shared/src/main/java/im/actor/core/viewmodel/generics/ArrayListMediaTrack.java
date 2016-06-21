package im.actor.core.viewmodel.generics;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.runtime.webrtc.WebRTCMediaTrack;

public class ArrayListMediaTrack extends ArrayList<WebRTCMediaTrack> {

    public ArrayListMediaTrack(int capacity) {
        super(capacity);
    }

    public ArrayListMediaTrack() {
    }

    public ArrayListMediaTrack(Collection<? extends WebRTCMediaTrack> collection) {
        super(collection);
    }

    @Override
    public WebRTCMediaTrack get(int index) {
        return super.get(index);
    }
}
