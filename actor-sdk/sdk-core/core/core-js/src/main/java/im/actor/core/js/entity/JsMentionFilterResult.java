package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import im.actor.core.entity.MentionFilterResult;
import im.actor.core.entity.Peer;
import im.actor.core.js.JsMessenger;
import im.actor.core.util.StringMatch;

public class JsMentionFilterResult extends JavaScriptObject {

    public static JsMentionFilterResult create(MentionFilterResult res) {
        JsMessenger messenger = JsMessenger.getInstance();
        JsPeerInfo peerInfo = messenger.buildPeerInfo(Peer.user(res.getUid()));

        JsArray<JsStringMatch> mentionMatches = JsArray.createArray().cast();
        JsArray<JsStringMatch> secondMatches = JsArray.createArray().cast();

        if (res.getMentionMatches() != null) {
            for (StringMatch match : res.getMentionMatches()) {
                mentionMatches.push(JsStringMatch.create(match));
            }
        }
        if (res.getOriginalMatches() != null) {
            for (StringMatch match : res.getOriginalMatches()) {
                secondMatches.push(JsStringMatch.create(match));
            }
        }

        return create(peerInfo, res.getMentionString(), mentionMatches, res.getOriginalString(),
                secondMatches, res.isNickname());
    }

    public static native JsMentionFilterResult create(JsPeerInfo peer,
                                                      String mentionText,
                                                      JsArray<JsStringMatch> mentionMatches,
                                                      String secondText,
                                                      JsArray<JsStringMatch> secondMatches,
                                                      boolean isNick)/*-{
        return {peer: peer, mentionText: mentionText, mentionMatches: mentionMatches, secondText: secondText, secondMatches: secondMatches, isNick: isNick };
    }-*/;

    protected JsMentionFilterResult() {

    }
}