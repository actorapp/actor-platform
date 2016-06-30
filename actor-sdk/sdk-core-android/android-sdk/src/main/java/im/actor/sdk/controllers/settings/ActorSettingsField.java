package im.actor.sdk.controllers.settings;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActorSettingsField {
    private int id;
    private View view = null;
    private int iconResourceId = 0;
    private String name = null;
    private View.OnClickListener onClickListener = null;
    private View rightView = null;
    private int iconColor = -1;
    private TextView nameTextView;
    private int rightViewWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
    private int rightViewHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

    public ActorSettingsField(int id) {
        this.id = id;
    }

    public View getView() {
        return view;
    }

    public ActorSettingsField setView(View view) {
        this.view = view;
        return this;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public ActorSettingsField setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ActorSettingsField setName(String name) {
        this.name = name;
        return this;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public ActorSettingsField setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public View getRightView() {
        return rightView;
    }

    public ActorSettingsField setRightView(View rightView) {
        this.rightView = rightView;
        return this;
    }

    public int getIconColor() {
        return iconColor;
    }

    public ActorSettingsField setIconColor(int iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public TextView getNameTextView() {
        return nameTextView;
    }

    public ActorSettingsField setNameTextView(TextView nameTextView) {
        this.nameTextView = nameTextView;
        return this;
    }

    public int getRightViewWidth() {
        return rightViewWidth;
    }

    public ActorSettingsField setRightViewWidth(int rightViewWidth) {
        this.rightViewWidth = rightViewWidth;
        return this;
    }

    public int getRightViewHeight() {
        return rightViewHeight;
    }

    public ActorSettingsField setRightViewHeight(int rightViewHeight) {
        this.rightViewHeight = rightViewHeight;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
