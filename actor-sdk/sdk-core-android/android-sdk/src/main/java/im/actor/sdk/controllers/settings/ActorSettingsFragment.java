package im.actor.sdk.controllers.settings;

import android.view.View;

import java.util.ArrayList;

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
    public View.OnClickListener getMenuFieldOnClickListener() {
        return null;
    }

    @Override
    public ActorSettingsCategories getBeforeSettingsCategories() {
        return null;
    }

    @Override
    public ActorSettingsCategories getAfterSettingsCategories() {
        return null;
    }
}
