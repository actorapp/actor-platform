package im.actor.sdk.controllers.settings;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;

public class ActorSettingsCategory {

    private int iconResourceId = 0;
    private int iconColor = -1;
    private String text;
    private ArrayList<ActorSettingsField> fields = new ArrayList();

    public ActorSettingsCategory(String text) {
        this.text = text;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public ActorSettingsCategory setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
        return this;
    }

    public int getIconColor() {
        return iconColor;
    }

    public ActorSettingsCategory setIconColor(int iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public ArrayList<ActorSettingsField> getFields() {
        return fields;
    }

    public ActorSettingsCategory setFields(ArrayList<ActorSettingsField> fields) {
        this.fields = fields;
        return this;
    }

    public ActorSettingsCategory addField(ActorSettingsField field) {
        fields.add(field);
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
