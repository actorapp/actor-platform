package im.actor.sdk.controllers.settings;

import android.content.Context;
import android.view.View;

public abstract class ActorSettingsCategory implements IActorSettingsCategory {

    private int iconResourceId = 0;
    private int iconColor = -1;


    @Override
    public View getView(Context context) {
        return null;
    }

    @Override
    public ActorSettingsField[] getFields() {
        return null;
    }

    @Override
    public int getIconResourceId() {
        return iconResourceId;
    }

    @Override
    public int getIconColor() {
        return iconColor;
    }
}
