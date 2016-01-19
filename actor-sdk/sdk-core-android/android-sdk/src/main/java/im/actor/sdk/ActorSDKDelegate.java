package im.actor.sdk;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import im.actor.core.AuthState;
import im.actor.core.entity.content.AbsContent;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.controllers.fragment.settings.BaseActorProfileActivity;
import im.actor.sdk.controllers.fragment.settings.BaseGroupInfoActivity;
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
     * @return Actor Intent
     */
    BaseActorProfileActivity getProfileIntent();

    /**
     * If not null returned, overrides group info activity intent
     *
     * @return Actor Intent
     */
    BaseGroupInfoActivity getGroupInfoIntent();

    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getChatSettingsIntent();

    /**
     * If not null returned, overrides settings activity intent
     *
     * @return Actor Intent
     */
    ActorIntentFragmentActivity getSecuritySettingsIntent();

    /**
     * If not null returned, overrides chat activity intent
     *
     * @return Actor Intent
     */
    ActorIntent getChatIntent();

    void onIncominCall(long callId, int uid);

    <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object... args);

    MainPhoneController getMainPhoneController(ActorMainActivity mainActivity);

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

    MessageHolder getCustomMessageViewHolder(int id, MessagesAdapter messagesAdapter, ViewGroup viewGroup);
}
