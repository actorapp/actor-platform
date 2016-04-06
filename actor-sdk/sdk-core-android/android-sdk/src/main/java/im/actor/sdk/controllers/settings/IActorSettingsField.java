package im.actor.sdk.controllers.settings;

import android.view.View;

public interface IActorSettingsField {
    String getName();

    int getIconResourceId();

    View.OnClickListener getOnclickListener();

    View getView();

    View getRightView();

    int getRightViewWidth();

    int getRightViewHeight();

    boolean addBottomDivider();

    int getIconColor();
}
