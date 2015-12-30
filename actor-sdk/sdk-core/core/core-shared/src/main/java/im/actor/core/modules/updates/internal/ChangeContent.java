package im.actor.core.modules.updates.internal;

import im.actor.core.api.updates.UpdateMessageContentChanged;

/**
 * Created by root on 12/30/15.
 */
public class ChangeContent extends InternalUpdate {
    UpdateMessageContentChanged update;

    public ChangeContent(UpdateMessageContentChanged update) {
        this.update = update;
    }

    public UpdateMessageContentChanged getUpdate() {
        return update;
    }
}
