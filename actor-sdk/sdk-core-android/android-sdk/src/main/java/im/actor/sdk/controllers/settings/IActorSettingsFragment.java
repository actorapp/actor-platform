package im.actor.sdk.controllers.settings;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

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

    ActorSettingsCategories getBeforeSettingsCategories();

    ActorSettingsCategories getAfterSettingsCategories();
}
