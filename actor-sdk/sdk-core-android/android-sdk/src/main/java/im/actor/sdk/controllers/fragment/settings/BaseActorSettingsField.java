package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 10/27/15.
 */
public abstract class BaseActorSettingsField implements ActorSettingsField {
    @Override
    public boolean addBottomDivider() {
        return true;
    }

    @Override
    public View getView(Context context) {
        return null;
    }

    @Override
    public int getIconResourceId() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public View.OnClickListener getOnclickListener() {
        return null;
    }

    @Override
    public View getRightView(Context context) {
        return null;
    }

    @Override
    public int getIconColor() {
        return -1;
    }
}
