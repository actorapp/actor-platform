package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

public interface IActorSettingsCategory {

    int getIconResourceId();

    int getIconColor();

    String getCategoryName();

    View getView(Context context);

    ActorSettingsField[] getFields();
}
