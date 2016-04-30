package im.actor.sdk.controllers.conversation.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.android.AndroidContext;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.view.BaseUrlSpan;

public class ReactionSpan extends BaseUrlSpan {

    private boolean hasMyReaction;
    Peer peer;
    long rid;
    String code;
    int baseColor;

    public ReactionSpan(String code, boolean hasMyReaction, Peer peer, long rid, int baseColor) {
        super(code, false);
        this.code = code;
        this.hasMyReaction = hasMyReaction;
        this.peer = peer;
        this.rid = rid;
        this.baseColor = baseColor;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(hasMyReaction && code.equals("\u2764") ? ActorSDK.sharedActor().style.getConvLikeColor() : baseColor);
        ds.setUnderlineText(false);
    }

    @Override
    public void onClick(View widget) {
        if (hasMyReaction) {
            ActorSDK.sharedActor().getMessenger().removeReaction(peer, rid, code).start(new CommandCallback<Void>() {
                @Override
                public void onResult(Void res) {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        } else {
            ActorSDK.sharedActor().getMessenger().addReaction(peer, rid, code).start(new CommandCallback<Void>() {
                @Override
                public void onResult(Void res) {

                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }

    public boolean hasMyReaction() {
        return hasMyReaction;
    }
}