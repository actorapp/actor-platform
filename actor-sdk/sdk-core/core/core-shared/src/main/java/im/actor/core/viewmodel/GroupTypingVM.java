/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.viewmodel;

import com.google.j2objc.annotations.Property;

import im.actor.runtime.mvvm.ValueModel;

/**
 * User Typing View Model
 */
public class GroupTypingVM {
    @Property("nonatomic, readonly")
    private int gid;
    @Property("nonatomic, readonly")
    private ValueModel<int[]> active;

    /**
     * <p>INTERNAL API</p>
     * Create Group Typing View Model
     *
     * @param gid group id
     */
    public GroupTypingVM(int gid) {
        this.gid = gid;
        this.active = new ValueModel<int[]>("groups." + gid + ".typing", new int[0]);
    }

    /**
     * Get Value Model's Group Id
     *
     * @return Group Id
     */
    public int getGid() {
        return gid;
    }

    /**
     * Get Value Model of active typing users
     *
     * @return ValueModel of int[] with user ids
     */
    public ValueModel<int[]> getActive() {
        return active;
    }
}
