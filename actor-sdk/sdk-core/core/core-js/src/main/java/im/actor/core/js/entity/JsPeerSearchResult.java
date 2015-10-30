package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

public class JsPeerSearchResult extends JavaScriptObject {

    public native static JsPeerSearchResult create(JsPeerInfo peerInfo, String description,
                                                   int membersCount, int created, JsPeerInfo creator,
                                                   boolean isPublic, boolean isJoined)/*-{
        return {peerInfo: peerInfo, description: description, membersCount: membersCount, created: created,
            isPublic: isPublic, isJoined: isJoined};
    }-*/;

    public native static JsPeerSearchResult create(JsPeerInfo peerInfo)/*-{
        return {peerInfo: peerInfo};
    }-*/;

    protected JsPeerSearchResult() {

    }
}
