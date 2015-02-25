package im.actor.model.modules;

import java.io.IOException;

import im.actor.model.entity.Group;
import im.actor.model.mvvm.MVVMCollection;
import im.actor.model.storage.KeyValueEngine;
import im.actor.model.viewmodel.GroupVM;

/**
 * Created by ex3ndr on 23.02.15.
 */
public class Groups extends BaseModule {

    private KeyValueEngine<Group> groups;
    private MVVMCollection<Group, GroupVM> collection;

    public Groups(Modules modules) {
        super(modules);
        collection = new MVVMCollection<Group, GroupVM>(modules.getConfiguration().getStorage().createGroupsEngine()) {
            @Override
            protected GroupVM createNew(Group raw) {
                return new GroupVM(raw);
            }

            @Override
            protected byte[] serialize(Group raw) {
                return raw.toByteArray();
            }

            @Override
            protected Group deserialize(byte[] raw) {
                try {
                    return Group.fromBytes(raw);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        groups = collection.getEngine();
    }

    public KeyValueEngine<Group> getGroups() {
        return groups;
    }

    public MVVMCollection<Group, GroupVM> getGroupsCollection() {
        return collection;
    }
}