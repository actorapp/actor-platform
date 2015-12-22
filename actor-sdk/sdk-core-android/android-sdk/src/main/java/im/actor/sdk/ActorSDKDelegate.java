package im.actor.sdk;

import android.content.Context;
import android.view.View;

import im.actor.core.AuthState;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.sdk.controllers.activity.controllers.MainPhoneController;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.intents.ActorIntent;

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
    ActorIntent getSettingsIntent();

    /**
     * If not null returned, overrides chat activity intent
     *
     * @return Actor Intent
     */
    ActorIntent getChatIntent();

    String getHelpPhone();

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
}
