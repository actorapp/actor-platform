package im.actor.core.modules.users;

import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.actors.Future;

class UsersUpdatesActor extends ModuleActor {

    public UsersUpdatesActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        // context().getUpdatesModule().
    }

    private void checkUsers(Integer[] uids, Future future) {
        for (int uid : uids) {
            if (users().getValue(uid) == null) {
                future.onResult(false);
                return;
            }
        }

        future.onResult(true);
    }

    @Override
    public boolean onAsk(Object message, Future future) {
        if (message instanceof CheckUsers) {
            checkUsers(((CheckUsers) message).uids, future);
            return false;
        } else {
            return super.onAsk(message, future);
        }
    }

    public static class CheckUsers {
        private Integer[] uids;

        public CheckUsers(Integer[] uids) {
            this.uids = uids;
        }
    }
}