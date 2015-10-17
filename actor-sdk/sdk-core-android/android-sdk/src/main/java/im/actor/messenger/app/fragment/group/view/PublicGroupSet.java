package im.actor.messenger.app.fragment.group.view;

import java.util.ArrayList;

import im.actor.core.entity.PublicGroup;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroupSet {
    private ArrayList<PublicGroup> groups;
    private String title;
    private String subtitle;

    public PublicGroupSet(ArrayList<PublicGroup> groups, String title, String subtitle) {
        this.groups = groups;
        this.title = title;
        this.subtitle = subtitle;
    }

    public ArrayList<PublicGroup> getGroups() {
        return groups;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getCount() {
        return groups.size();
    }
}
