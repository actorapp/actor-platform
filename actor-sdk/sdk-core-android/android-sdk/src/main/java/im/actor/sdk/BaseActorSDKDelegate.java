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
import im.actor.sdk.controllers.fragment.auth.SignPhoneFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.intents.ActorIntent;
import im.actor.sdk.intents.ActorIntentFragmentActivity;

/**
 * Base Implementation of Actor SDK Delegate. This class is recommended to subclass instead
 * of implementing ActorSDKDelegate
 */
public class BaseActorSDKDelegate implements ActorSDKDelegate {

    //
    // Authentication Activity
    //

    @Deprecated
    @Override
    public AuthState getAuthStartState() {
        return AuthState.AUTH_START;
    }

    @Override
    public BaseAuthFragment getSignFragment() {
        return new SignPhoneFragment();
    }

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
    public ActorIntent getChatIntent() {
        return null;
    }

    @Override
    public String getHelpPhone() {
        return "75551234567";
    }

    @Override
    public <T extends BindedViewHolder, J extends T> J getViewHolder(Class<T> base, Object[] args) {
        return null;
    }

    @Override
    public MainPhoneController getMainPhoneController(ActorMainActivity mainActivity) {
        return null;
    }

    //
    // Hacking settings activity
    //

    @Deprecated
    @Override
    public View getBeforeNickSettingsView(Context context) {
        return null;
    }

    @Deprecated
    @Override
    public View getAfterPhoneSettingsView(Context context) {
        return null;
    }

    @Deprecated
    @Override
    public View getSettingsTopView(Context context) {
        return null;
    }

    @Deprecated
    @Override
    public View getSettingsBottomView(Context context) {
        return null;
    }

    @Deprecated
    @Override
    public ActorSettingsCategory[] getBeforeSettingsCategories() {
        return null;
    }

    @Deprecated
    @Override
    public ActorSettingsCategory[] getAfterSettingsCategories() {
        return null;
    }

    @Override
    public MessageHolder getCustomMessageViewHolder(Class<AbsContent> content, MessagesAdapter messagesAdapter, ViewGroup viewGroup) {
        return null;
    }


}
