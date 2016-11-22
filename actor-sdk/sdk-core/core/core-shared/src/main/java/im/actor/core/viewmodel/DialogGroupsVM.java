package im.actor.core.viewmodel;

import java.util.ArrayList;

import im.actor.runtime.mvvm.ValueModel;

public class DialogGroupsVM {
    
    private ValueModel<ArrayList<DialogGroup>> groupsValueModel;

    public DialogGroupsVM() {
        groupsValueModel = new ValueModel<ArrayList<DialogGroup>>("groups.model", null);
    }

    public ValueModel<ArrayList<DialogGroup>> getGroupsValueModel() {
        return groupsValueModel;
    }
}
