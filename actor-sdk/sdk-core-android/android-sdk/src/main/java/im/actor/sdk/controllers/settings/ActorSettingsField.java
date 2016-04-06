package im.actor.sdk.controllers.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActorSettingsField implements IActorSettingsField {
    private boolean addBottomDivider = true;
    private View view = null;
    private int iconResourceId = 0;
    private String name = null;
    private View.OnClickListener onClickListener = null;
    private View rightView = null;
    private int iconColor = -1;
    private TextView nameTextView;

    public ActorSettingsField() {
    }

    public ActorSettingsField(boolean addBottomDivider) {
        this.addBottomDivider = addBottomDivider;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener) {
        this.name = name;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener, boolean addBottomDivider) {
        this.name = name;
        this.addBottomDivider = addBottomDivider;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener, int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener, int iconResourceId, boolean addBottomDivider) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.addBottomDivider = addBottomDivider;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener, int iconResourceId, int iconColor) {
        this.name = name;
        this.iconColor = iconColor;
        this.iconResourceId = iconResourceId;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener, int iconResourceId, int iconColor, boolean addBottomDivider) {
        this.addBottomDivider = addBottomDivider;
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.iconColor = iconColor;
        this.onClickListener = onClickListener;
    }


    @Override
    public boolean addBottomDivider() {
        return addBottomDivider;
    }

    @Override
    public int getIconResourceId() {
        return iconResourceId;
    }

    public ActorSettingsField setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public ActorSettingsField setName(String name) {
        this.name = name;
        if (nameTextView != null) {
            nameTextView.setText(name);
        }
        return this;
    }

    @Override
    public View.OnClickListener getOnclickListener() {
        return onClickListener;
    }

    @Override
    public View getRightView() {
        return rightView;
    }

    @Override
    public int getRightViewWidth() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public int getRightViewHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }


    @Override
    public int getIconColor() {
        return iconColor;
    }

    public ActorSettingsField setIconColor(int iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public ActorSettingsField addBottomDivider(boolean addBottomDivider) {
        this.addBottomDivider = addBottomDivider;
        return this;
    }

    public View getView() {
        return view;
    }

    public ActorSettingsField setView(View view) {
        this.view = view;
        return this;
    }

    public ActorSettingsField setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

}
