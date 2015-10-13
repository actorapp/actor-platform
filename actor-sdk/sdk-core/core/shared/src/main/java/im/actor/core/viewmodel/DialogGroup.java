package im.actor.core.viewmodel;

import java.util.ArrayList;

import im.actor.core.entity.DialogDesc;

public class DialogGroup {

    private String title;
    private String key;
    private ArrayList<DialogDesc> dialogs;

    public DialogGroup(String title, String key, ArrayList<DialogDesc> dialogs) {
        this.title = title;
        this.key = key;
        this.dialogs = dialogs;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public ArrayList<DialogDesc> getDialogs() {
        return dialogs;
    }
}
