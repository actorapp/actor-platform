package im.actor.sdk;

import android.content.Context;
import android.view.View;

import im.actor.core.AuthState;
import im.actor.sdk.controllers.fragment.auth.BaseAuthFragment;
import im.actor.sdk.controllers.fragment.settings.ActorSettingsCategory;
import im.actor.sdk.intents.ActorIntent;

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

    AuthState getAuthStartState();

    BaseAuthFragment getSignFragment();

    View getBeforeNickSettingsView(Context context);

    View getAfterPhoneSettingsView(Context context);

    View getSettingsTopView(Context context);

    View getSettingsBottomView(Context context);

    ActorSettingsCategory getBeforeSettingsCategory();

    ActorSettingsCategory getAfterSettingsCategory();
}
