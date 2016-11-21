package im.actor.sdk;

import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.RawUpdatesHandler;
import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.ChatFragment;
import im.actor.sdk.controllers.conversation.attach.AbsAttachFragment;
import im.actor.sdk.controllers.conversation.inputbar.InputBarFragment;
import im.actor.sdk.controllers.conversation.mentions.AutocompleteFragment;
import im.actor.sdk.controllers.conversation.messages.BubbleLayouter;
import im.actor.sdk.controllers.conversation.quote.QuoteFragment;
import im.actor.sdk.controllers.dialogs.DialogsDefaultFragment;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

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

    @Nullable
    @Override
    public Fragment fragmentForCall(long callId) {
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

    public Uri getNotificationSoundForPeer(Peer peer) {

        String globalSound = messenger().getPreferences().getString("userNotificationSound_" + peer.getPeerId());
        if (globalSound != null && !globalSound.equals("none")) {
            return Uri.parse(globalSound);
        }

        return getNotificationSound();
    }

    public int getNotificationColorForPeer(Peer peer) {
        return getNotificationColor();
    }

    public Uri getNotificationSound() {
        String globalSound = messenger().getPreferences().getString("globalNotificationSound");
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

    @Override
    public RawUpdatesHandler getRawUpdatesHandler() {
        return null;
    }

    @Override
    public void configureChatViewHolders(ArrayList<BubbleLayouter> layouters) {
    }

    @Override
    public DialogsDefaultFragment fragmentForDialogs() {
        return null;
    }
}
