package im.actor.sdk.controllers.conversation.messages;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.view.BaseUrlSpan;

public class ReactionSpan extends BaseUrlSpan {

    boolean hasMyReaction;
    Peer peer;
    long rid;
    String code;

    public ReactionSpan(String code, boolean hasMyReaction, Peer peer, long rid) {
        super(code, false);
        this.code = code;
        this.hasMyReaction = hasMyReaction;
        this.peer = peer;
        this.rid = rid;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(hasMyReaction && code.equals("\u2764") ? ActorSDK.sharedActor().style.getConvLikeColor() : ActorSDK.sharedActor().style.getConvTimeColor());
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        if (hasMyReaction) {
            ActorSDK.sharedActor().getMessenger().removeReaction(peer, rid, code).start(new CommandCallback<Boolean>() {
                @Override
                public void onResult(Boolean res) {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            ActorSDK.sharedActor().getMessenger().addReaction(peer, rid, code).start(new CommandCallback<Boolean>() {
                @Override
                public void onResult(Boolean res) {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }
}