package im.actor.model.modules.updates;

import java.util.ArrayList;
import java.util.Collection;

import im.actor.model.entity.Group;
import im.actor.model.modules.BaseModule;
import im.actor.model.modules.Modules;
import im.actor.model.modules.messages.DialogsActor;
import im.actor.model.modules.messages.entity.EntityConverter;

import static im.actor.model.util.JavaUtil.equalsE;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class GroupsProcessor extends BaseModule {

    public GroupsProcessor(Modules modules) {
        super(modules);
    }

    public void applyGroups(Collection<im.actor.model.api.Group> updated, boolean forced) {
        ArrayList<Group> batch = new ArrayList<Group>();
        for (im.actor.model.api.Group group : updated) {
            Group saved = groups().getValue(group.getId());
            if (saved == null) {
                batch.add(EntityConverter.convert(group));
            } else if (forced) {
                Group upd = EntityConverter.convert(group);
                batch.add(upd);

                // Sending changes to dialogs
                if (!equalsE(upd.getAvatar(), saved.getAvatar()) ||
                        !upd.getTitle().equals(saved.getTitle())) {
                    modules().getMessagesModule().getDialogsActor()
                            .send(new DialogsActor.GroupChanged(upd));
                }
            }
        }

        if (batch.size() > 0) {
            groups().addOrUpdateItems(batch);
        }
    }

    public boolean hasGroups(Collection<Integer> gids) {
        for (Integer uid : gids) {
            if (groups().getValue(uid) == null) {
                return false;
            }
        }
        return true;
    }
}