package im.actor.model.entity;

import org.jetbrains.annotations.Nullable;

import im.actor.model.droidkit.engine.ListEngineItem;

/**
 * Created by korka on 30.06.15.
 */
public class PublicGroup extends im.actor.model.api.PublicGroup implements ListEngineItem {

    public PublicGroup(im.actor.model.api.PublicGroup raw) {
        super(raw.getId(), raw.getAccessHash(), raw.getTitle(), raw.getAvatar(), raw.getMembersCount(), raw.getFriendsCount(), raw.getDescription());
    }

    @Override
    public long getEngineId() {
        return getId();
    }

    @Override
    public long getEngineSort() {
        return getId();
    }

    @Nullable
    @Override
    public String getEngineSearch() {
        return getTitle();
    }
}
