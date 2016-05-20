package im.actor.sdk.controllers.settings;

import android.view.View;

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
