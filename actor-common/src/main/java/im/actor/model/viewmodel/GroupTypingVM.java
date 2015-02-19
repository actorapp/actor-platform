package im.actor.model.viewmodel;

import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class GroupTypingVM {
    private int gid;
    private ValueModel<int[]> active;

    public GroupTypingVM(int gid) {
        this.gid = gid;
        this.active = new ValueModel<int[]>("groups." + gid + ".typing", new int[0]);
    }

    public int getGid() {
        return gid;
    }

    public ValueModel<int[]> getActive() {
        return active;
    }
}
