package im.actor.model.modules.groups;

import java.util.HashMap;

import im.actor.model.entity.FileLocation;
import im.actor.model.modules.Modules;
import im.actor.model.modules.utils.ModuleActor;
import im.actor.model.modules.utils.RandomUtils;

/**
 * Created by ex3ndr on 04.03.15.
 */
public class GroupAvatarChangeActor extends ModuleActor {

    private HashMap<Integer, Long> currentTasks = new HashMap<Integer, Long>();

    public GroupAvatarChangeActor(Modules modules) {
        super(modules);
    }

    public void changeAvatar(int gid, String descriptor) {
        if (currentTasks.containsKey(gid)) {
            modules().getFilesModule().cancelUpload(currentTasks.get(gid));
            currentTasks.remove(gid);
        }
        long rid = RandomUtils.nextRid();
        currentTasks.put(gid, rid);

        modules().getFilesModule().requestUpload(rid, descriptor, "avatar.jpg", self());
    }

    public void uploadCompleted(final long rid, FileLocation fileLocation) {
        
    }
}
