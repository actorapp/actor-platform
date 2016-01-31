package im.actor.core.modules.users;

import im.actor.core.modules.ModuleContext;
import im.actor.core.util.ModuleActor;
import im.actor.runtime.promise.PromiseResolver;

class UsersUpdatesActor extends ModuleActor {

    public UsersUpdatesActor(ModuleContext context) {
        super(context);
    }

    @Override
    public void preStart() {
        // context().getUpdatesModule().
    }

    private void checkUsers(Integer[] uids, PromiseResolver future) {
        for (int uid : uids) {
            if (users().getValue(uid) == null) {
                future.result(false);
                return;
            }
        }

        future.result(true);
    }

    @Override
    public void onAsk(Object message, PromiseResolver future) {
        if (message instanceof CheckUsers) {
            checkUsers(((CheckUsers) message).uids, future);
        } else {
            super.onAsk(message, future);
        }
    }

    public static class CheckUsers {
        private Integer[] uids;

        public CheckUsers(Integer[] uids) {
            this.uids = uids;
        }
    }
}