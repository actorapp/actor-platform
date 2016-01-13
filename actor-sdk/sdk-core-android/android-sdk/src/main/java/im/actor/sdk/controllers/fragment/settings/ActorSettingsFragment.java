package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 11/12/15.
 */
public class ActorSettingsFragment extends BaseActorSettingsFragment {
    @Override
    public View getBeforeNickSettingsView() {
        return null;
    }

    @Override
    public View getAfterPhoneSettingsView() {
        return null;
    }

    @Override
    public View getSettingsTopView() {
        return null;
    }

    @Override
    public View getSettingsBottomView() {
        return null;
    }

    @Override
    public boolean showWallpaperCategory() {
        return true;
    }

    @Override
    public boolean showAskQuestion() {
        return true;
    }

    @Override
    public ActorSettingsCategory[] getBeforeSettingsCategories() {
        return null;
    }

    @Override
    public ActorSettingsCategory[] getAfterSettingsCategories() {
        return null;
    }
}
