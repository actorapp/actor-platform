package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 11/11/15.
 */
public interface IActorSettingsFragment {
    View getBeforeNickSettingsView();

    View getAfterPhoneSettingsView();

    View getSettingsTopView();

    View getSettingsBottomView();

    boolean showWallpaperCategory();

    ActorSettingsCategory[] getBeforeSettingsCategories();

    ActorSettingsCategory[] getAfterSettingsCategories();
}
