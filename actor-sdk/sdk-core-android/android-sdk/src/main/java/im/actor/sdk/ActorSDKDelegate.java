package im.actor.sdk;

import android.app.Activity;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.TableLayout;

import im.actor.core.entity.Peer;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.root.MainPhoneController;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.settings.BaseGroupInfoActivity;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

/**
 * SDK Delegate. Used for delegating some work to
 */
public interface ActorSDKDelegate {

    /**
     * Intent for first authentication Activity. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getAuthStartIntent();

    /**
     * Intent for activity that is launched after user log in. For start activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartAfterLoginIntent();

    /**
     * Intent for application start activity for logged in user. For default activity return null.
     *
     * @return Actor Intent
     */
    ActorIntent getStartIntent();

    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getSettingsIntent();

    /**
     * If not null returned, overrides users profile activity intent
     *
     * @param uid user id
     * @return Actor Intent
     */
    BaseActorProfileActivity getProfileIntent(int uid);

    /**
     * If not null returned, overrides group info activity intent
     *
     * @return Actor Intent
     */
    BaseGroupInfoActivity getGroupInfoIntent(int gid);

    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getChatSettingsIntent();

    /**
     * If not null returned, overrides security settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getSecuritySettingsIntent();

    /**
     * If not null returned, overrides chat activity intent
     *
     * @param peer    chat peer
     * @param compose pop up keyboard at start
     * @return Actor Intent
     */
    ActorIntent getChatIntent(Peer peer, boolean compose);

    /**
     * Override for handling incoming call
     *
     * @param callId call id
     * @param uid    caller user id
     */
    void onIncomingCall(long callId, int uid);

    /**
     * Override for hacking default messages view holders
     *
     * @param base base view holder class
     * @param args args passed to view holder
     * @param <T>  base view holder class
     * @param <J>  return class
     * @return hacked view holder
     */
    <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object... args);

    /**
     * Override for hacking MainPhoneController - activity with chats/contacts
     *
     * @param mainActivity main activity
     * @return hacked MainPhoneController
     */
    MainPhoneController getMainPhoneController(ActorMainActivity mainActivity);

    /**
     * Override for hacking custom messages view holders
     *
     * @param dataTypeHash    json dataType hash
     * @param messagesAdapter adapter to pass to holder
     * @param viewGroup       ViewGroup to pass to holder
     * @return custom view holder
     */
    MessageHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup);

    /**
     * Return True if custom share menu is clicked
     *
     * @param activity called from activity
     * @return true if custom share menu shown
     */
    boolean onAttachMenuClicked(Activity activity);

    /**
     * Override for setting specific notification sound for peer
     *
     * @param peer peer to set notification sound
     * @return notification sound uri
     */
    Uri getNotificationSoundForPeer(Peer peer);


    /**
     * Override for setting specific notification color for peer
     *
     * @param peer peer to set notification color
     * @return notification sound color
     */
    int getNotificationColorForPeer(Peer peer);


    /**
     * Override change notification sound
     *
     * @return notification sound uri
     */
    Uri getNotificationSound();

    /**
     * Override change notification color
     *
     * @return notification sound color
     */
    int getNotificationColor();


    /**
     * Is Actor pushes used for this app - added for testing
     *
     * @return is Actor push id used
     */
    boolean useActorPush();

    /**
     * Method for hacking share menu in dialog
     *
     * @param shareMenu share menu
     */
    void onShareMenuCreated(TableLayout shareMenu);
}
