package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 11/11/15.
 */
public interface IActorSettingsFragment {
    View getBeforeNickSettingsView(Context context);

    View getAfterPhoneSettingsView(Context context);

    View getSettingsTopView(Context context);

    View getSettingsBottomView(Context context);

    ActorSettingsCategory[] getBeforeSettingsCategories();

    ActorSettingsCategory[] getAfterSettingsCategories();
}
