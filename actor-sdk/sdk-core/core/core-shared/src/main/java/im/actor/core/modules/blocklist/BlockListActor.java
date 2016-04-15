package im.actor.core.modules.blocklist;

import im.actor.core.modules.ModuleActor;
import im.actor.core.modules.ModuleContext;

public class BlockListActor extends ModuleActor {

    public BlockListActor(ModuleContext context) {
        super(context);
    }

    public void onBlocked(int uid) {
        context().getBlockList().markBlocked(uid);
    }

    public void onUnblocked(int uid) {
        context().getBlockList().markNonBlocked(uid);
    }


    //
    // Messages
    //

    @Override
    public void onReceive(Object message) {
        if (message instanceof UserBlocked) {
            onBlocked(((UserBlocked) message).getUid());
        } else if (message instanceof UserUnblocked) {
            onUnblocked(((UserUnblocked) message).getUid());
        } else {
            super.onReceive(message);
        }
    }

    public static class UserBlocked {

        private int uid;

        public UserBlocked(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }

    public static class UserUnblocked {
        private int uid;

        public UserUnblocked(int uid) {
            this.uid = uid;
        }

        public int getUid() {
            return uid;
        }
    }
}