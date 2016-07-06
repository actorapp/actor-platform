package im.actor.sdk;

import android.app.Activity;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.TableLayout;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.conversation.ShareMenuField;
import im.actor.sdk.controllers.conversation.messages.content.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

/**
 * Base Implementation of Actor SDK Delegate. This class is recommended to subclass instead
 * of implementing ActorSDKDelegate
 */
public class BaseActorSDKDelegate implements ActorSDKDelegate {

    /**
     * Return non-null to open specific Activity for starting auth.
     * If null is specified, result AuthActivity is used.
     *
     * @return ActorIntent for auth activity
     */
    @Override
    public ActorIntent getAuthStartIntent() {
        return null;
    }

    //
    // Starting Activity
    //

    /**
     * Return non-null to open specific Activity after user's successful log in.
     * If null is specified, result of getStartIntent is used.
     *
     * @return ActorIntent for activity after login
     */
    @Override
    public ActorIntent getStartAfterLoginIntent() {
        return null;
    }

    /**
     * Return non-null to open specific Activity when user is logged in. If null, SDK will launch
     * standard Messaging activity with contacts and recent list
     *
     * @return ActorIntent for start activity
     */
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

    /**
     * Return non-null to open specific setting Activity. If null, SDK will launch
     * standard Settings activity
     *
     * @return ActorIntent for settings activity
     */
    @Override
    public ActorIntentFragmentActivity getSettingsIntent() {
        return null;
    }

    /**
     * Return non-null to open specific user group info Activity. If null, SDK will launch
     * standard group info activity
     *
     * @param gid group id
     * @return ActorIntent for group info activity
     */
    @Override
    public BaseGroupInfoActivity getGroupInfoIntent(int gid) {
        return null;
    }

    /**
     * Return non-null to open specific user settings Activity. If null, SDK will launch
     * standard settings activity
     *
     * @return ActorIntent for group info activity
     */
    @Override
    public ActorIntentFragmentActivity getChatSettingsIntent() {
        return null;
    }

    /**
     * Return non-null to open specific user settings Activity. If null, SDK will launch
     * standard settings activity
     *
     * @return ActorIntent for group info activity
     */
    @Override
    public ActorIntentFragmentActivity getSecuritySettingsIntent() {
        return null;
    }


    /**
     * Return non-null to open specific chat Activity. If null, SDK will launch
     * standard chat activity
     *
     * @param peer    chat peer
     * @param compose pop up keyboard at start
     * @return ActorIntent for chat activity
     */
    @Override
    public ActorIntent getChatIntent(Peer peer, boolean compose) {
        return null;
    }

    /**
     * Override for hacking default messages view holders
     *
     * @param base base view holder class
     * @param args args passed to view holder
     * @param <T>  base view holder class
     * @param <J>  return class
     * @return hacked view holder
     */
    @Override
    public <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object[] args) {
        return null;
    }

    /**
     * Override for hacking custom messages view holders
     *
     * @param dataTypeHash    id in same order as added to AbsContent.registerConverter()
     * @param messagesAdapter adapter to pass to holder
     * @param viewGroup       ViewGroup to pass to holder
     * @return custom view holder
     */
    @Override
    public MessageHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean onAttachMenuClicked(Activity activity) {
        return false;
    }

    /**
     * Method for hacking share menu in dialog
     *
     * @param shareMenu share menu
     */
    @Override
    public void onShareMenuCreated(TableLayout shareMenu) {

    }

    @Override
    public void addCustomShareMenuFields(ArrayList<ShareMenuField> menuFields) {
    }

    //
    // Notifications
    //

    public Uri getNotificationSoundForPeer(Peer peer) {
        return getNotificationSound();
    }

    public int getNotificationColorForPeer(Peer peer) {
        return getNotificationColor();
    }

    public Uri getNotificationSound() {
        return Settings.System.DEFAULT_NOTIFICATION_URI;
    }

    public int getNotificationColor() {
        return ActorSDK.sharedActor().style.getMainColor();
    }
}
