package im.actor.sdk.util;

import im.actor.core.AndroidMessenger;
import im.actor.core.entity.Group;
import im.actor.core.entity.User;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.sdk.ActorSDK;

public class ActorSDKMessenger {

    public static AndroidMessenger messenger() {
        return ActorSDK.sharedActor().getMessenger();
    }

    public static MVVMCollection<User, UserVM> users() {
        ActorSDK.sharedActor().waitForReady();
        return messenger().getUsers();
    }

    public static MVVMCollection<Group, GroupVM> groups() {
        return messenger().getGroups();
    }

    public static int myUid() {
        ActorSDK.sharedActor().waitForReady();
        return messenger().myUid();
    }
}
