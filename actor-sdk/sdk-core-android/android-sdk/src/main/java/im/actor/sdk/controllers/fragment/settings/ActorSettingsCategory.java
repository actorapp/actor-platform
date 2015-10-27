package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

public interface ActorSettingsCategory {
    String getCategoryName();
    View getView(Context context);

    BaseActorSettingsField[] getFields();
}
