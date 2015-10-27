package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

/**
 * Created by root on 10/27/15.
 */
public abstract class ActorSettingsCategory implements IActorSettingsCategory {
    @Override
    public View getView(Context context) {
        return null;
    }

    @Override
    public ActorSettingsField[] getFields() {
        return null;
    }
}
