package im.actor.sdk.controllers.settings;

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

    boolean showAskQuestion();

    ActorSettingsCategory[] getBeforeSettingsCategories();

    ActorSettingsCategory[] getAfterSettingsCategories();
}
