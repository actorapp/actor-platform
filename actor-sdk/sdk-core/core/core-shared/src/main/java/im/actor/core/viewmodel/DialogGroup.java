package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.core.viewmodel.generics.ArrayListDialogSmall;

public class DialogGroup {

    @Property("readonly, nonatomic")
    private String title;
    @Property("readonly, nonatomic")
    private String key;
    @Property("readonly, nonatomic")
    private ArrayListDialogSmall dialogs;

    public DialogGroup(String title, String key, ArrayListDialogSmall dialogs) {
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

    public ArrayListDialogSmall getDialogs() {
        return dialogs;
    }
}
