package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

public interface IActorSettingsField {
    String getName();

    int getIconResourceId();

    View.OnClickListener getOnclickListener();

    View getView();

    View getRightView();

    boolean addBottomDivider();

    int getIconColor();
}
