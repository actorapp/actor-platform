package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 10/27/15.
 */
public abstract class BaseActorSettingsCategory implements ActorSettingsCategory {
    @Override
    public View getView(Context context) {
        return null;
    }

    @Override
    public BaseActorSettingsField[] getFields() {
        return null;
    }
}
