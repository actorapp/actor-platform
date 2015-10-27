package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

public interface IActorSettingsCategory {
    String getCategoryName();

    View getView(Context context);

    ActorSettingsField[] getFields();
}
