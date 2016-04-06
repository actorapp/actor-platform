package im.actor.sdk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import im.actor.core.AuthState;
import im.actor.core.entity.Peer;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.auth.BaseAuthFragment;
import im.actor.sdk.controllers.settings.ActorSettingsCategory;
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
     * @param uid   user id
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
     * @return Actor Intent
     * @param peer      chat peer
     * @param compose   pop up keyboard at start
     */
    ActorIntent getChatIntent(Peer peer, boolean compose);

    /**
     * Override for handling incoming call
     *
     * @param callId call id
     * @param uid    caller user id
     */
    void onIncominCall(long callId, int uid);

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
     * @param dataTypeHash      json dataType hash
     * @param messagesAdapter   adapter to pass to holder
     * @param viewGroup         ViewGroup to pass to holder
     * @return custom view holder
     */
    MessageHolder getCustomMessageViewHolder(int dataTypeHash, MessagesAdapter messagesAdapter, ViewGroup viewGroup);



    /**
     * Is Actor pushes used for this app - added for testing
     *
     * @return is Actor push id used
     */
    boolean useActorPush();

    @Deprecated
    AuthState getAuthStartState();

    @Deprecated
    BaseAuthFragment getSignFragment();

    @Deprecated
    View getBeforeNickSettingsView(Context context);

    @Deprecated
    View getAfterPhoneSettingsView(Context context);

    @Deprecated
    View getSettingsTopView(Context context);

    @Deprecated
    View getSettingsBottomView(Context context);

    @Deprecated
    ActorSettingsCategory[] getBeforeSettingsCategories();

    @Deprecated
    ActorSettingsCategory[] getAfterSettingsCategories();
}
