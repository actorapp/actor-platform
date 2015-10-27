package im.actor.sdk.controllers.fragment.settings;

import android.content.Context;
import android.view.View;

public class ActorSettingsField implements IActorSettingsField {
    private boolean addBottomDivider = true;
    private View view = null;
    private int iconResourceId = 0;
    private String name = null;
    private View.OnClickListener onClickListener = null;
    private View rightView = null;
    private int iconColor = -1;

    public ActorSettingsField() {
    }

    public ActorSettingsField(boolean addBottomDivider) {
        this.addBottomDivider = addBottomDivider;
    }

    public ActorSettingsField(String name, View.OnClickListener onClickListener) {
        this.name = name;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, boolean addBottomDivider, View.OnClickListener onClickListener) {
        this.name = name;
        this.addBottomDivider = addBottomDivider;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, int iconResourceId, View.OnClickListener onClickListener) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, int iconResourceId, boolean addBottomDivider, View.OnClickListener onClickListener) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.addBottomDivider = addBottomDivider;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(String name, int iconColor, int iconResourceId, View.OnClickListener onClickListener) {
        this.name = name;
        this.iconColor = iconColor;
        this.iconResourceId = iconResourceId;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(boolean addBottomDivider, int iconResourceId, String name, int iconColor, View.OnClickListener onClickListener) {
        this.addBottomDivider = addBottomDivider;
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.iconColor = iconColor;
        this.onClickListener = onClickListener;
    }

    public ActorSettingsField(int iconResourceId, String name, View.OnClickListener onClickListener, View rightView, int iconColor) {
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.onClickListener = onClickListener;
        this.rightView = rightView;
        this.iconColor = iconColor;
    }

    public ActorSettingsField(int iconResourceId, String name, View.OnClickListener onClickListener, View rightView, int iconColor, boolean addBottomDivider) {
        this.iconResourceId = iconResourceId;
        this.name = name;
        this.onClickListener = onClickListener;
        this.rightView = rightView;
        this.iconColor = iconColor;
        this.addBottomDivider = addBottomDivider;
    }

    public ActorSettingsField(View view, View.OnClickListener onClickListener) {
        this.view = view;
        this.onClickListener = onClickListener;
    }


    @Override
    public boolean addBottomDivider() {
        return addBottomDivider;
    }

    @Override
    public View getView(Context context) {
        return view;
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
        return this;
    }

    @Override
    public View.OnClickListener getOnclickListener() {
        return onClickListener;
    }

    @Override
    public View getRightView(Context context) {
        return rightView;
    }

    public View getRightView() {
        return rightView;
    }

    public ActorSettingsField setRightView(View rightView) {
        this.rightView = rightView;
        return this;
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

    public ActorSettingsField setView(View view) {
        this.view = view;
        return this;
    }

    public ActorSettingsField setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
}
