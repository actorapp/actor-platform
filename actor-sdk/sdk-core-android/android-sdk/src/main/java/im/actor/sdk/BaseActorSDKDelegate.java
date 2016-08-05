package im.actor.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import org.jetbrains.annotations.Nullable;

import im.actor.core.entity.Peer;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.conversation.ChatFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteFragment;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.MessageHolder;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

/**
 * Base Implementation of Actor SDK Delegate. This class is recommended to subclass instead
 * of implementing ActorSDKDelegate
 */
public class BaseActorSDKDelegate implements ActorSDKDelegate {

    @Override
    public ActorIntent getAuthStartIntent() {
        return null;
    }

    @Override
    public ActorIntent getStartAfterLoginIntent() {
        return null;
    }

    @Override
    public ActorIntent getStartIntent() {
        return null;
    }

    @Nullable
    @Override
    public Fragment fragmentForRoot() {
        return null;
    }

    @Nullable
    @Override
    public Fragment fragmentForProfile(int uid) {
        return null;
    }

    @Override
    public Fragment fragmentForGroupInfo(int gid) {
        return null;
    }

    @Nullable
    @Override
    public AbsAttachFragment fragmentForAttachMenu(Peer peer) {
        return null;
    }

    @Nullable
    @Override
    public ChatFragment fragmentForChat(Peer peer) {
        return null;
    }

    @Nullable
    @Override
    public InputBarFragment fragmentForChatInput() {
        return null;
    }

    @Override
    public AutocompleteFragment fragmentForAutocomplete(Peer peer) {
        return null;
    }

    @Override
    public QuoteFragment fragmentForQuote() {
        return null;
    }

    @Nullable
    @Override
    public Fragment fragmentForToolbar(Peer peer) {
        return null;
    }

    @Override
    public ActorIntentFragmentActivity getSettingsIntent() {
        return null;
    }

    @Override
    public ActorIntentFragmentActivity getChatSettingsIntent() {
        return null;
    }

    @Override
    public ActorIntentFragmentActivity getSecuritySettingsIntent() {
        return null;
    }

    @Override
    public ActorIntent getChatIntent(Peer peer, boolean compose) {
        return null;
    }

    @Override
    public <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object[] args) {
        return null;
    }

    @Override
    public MessageHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
        return null;
    }

    public Uri getNotificationSoundForPeer(Peer peer) {
        SharedPreferences sharedPreferences = AndroidContext.getContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);

        String globalSound = sharedPreferences.getString("userSound_" + peer.getPeerId(), null);
        if (globalSound != null) {
            if (globalSound.equals("none")) {
                return null;
            } else {
                return Uri.parse(globalSound);
            }
        }

        return getNotificationSound();
    }

    public int getNotificationColorForPeer(Peer peer) {
        return getNotificationColor();
    }

    public Uri getNotificationSound() {
        SharedPreferences sharedPreferences = AndroidContext.getContext().getSharedPreferences("notifications", Context.MODE_PRIVATE);

        String globalSound = sharedPreferences.getString("globalSound", null);
        if (globalSound != null) {
            if (globalSound.equals("none")) {
                return null;
            } else {
                return Uri.parse(globalSound);
            }
        }

        return Settings.System.DEFAULT_NOTIFICATION_URI;
    }

    public int getNotificationColor() {
        return ActorSDK.sharedActor().style.getMainColor();
    }
}
