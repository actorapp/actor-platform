package im.actor.messenger.core.actors.groups;


import com.droidkit.actors.concurrency.FutureCallback;

import im.actor.api.scheme.GroupOutPeer;
import im.actor.api.scheme.rpc.ResponseSeq;
import im.actor.api.scheme.rpc.ResponseSeqDate;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.PendingActor;
import im.actor.messenger.storage.DbProvider;
import im.actor.messenger.util.RandomUtil;

import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class GroupLeaveActor extends PendingActor<PendingUserAction> {

    public GroupLeaveActor() {
        super("group", DbProvider.getDatabase(AppContext.getContext()));
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof LeaveChat) {
            LeaveChat leaveChat = (LeaveChat) message;
            addAction(new PendingUserAction(leaveChat.chatId, leaveChat.chatAccessHash, PendingUserAction.ACTION_LEAVE, 0, 0));
        }
    }

    @Override
    protected void performAction(final PendingUserAction action) {
        performLeave(action);
    }

    private void performLeave(final PendingUserAction action) {

        ask(requests().leaveGroup(new GroupOutPeer(action.getChatId(), action.getChatAccessHash()), RandomUtil.randomId()), new FutureCallback<ResponseSeqDate>() {
            @Override
            public void onResult(ResponseSeqDate result) {
                // Success chat leave
                onActionCompleted(action);
            }

            @Override
            public void onError(Throwable throwable) {
                // Just ignore this
                throwable.printStackTrace();
                onActionCompleted(action);
            }
        });
    }

    public static class LeaveChat {
        private int chatId;
        private long chatAccessHash;

        public LeaveChat(int chatId, long chatAccessHash) {
            this.chatId = chatId;
            this.chatAccessHash = chatAccessHash;
        }
    }
}
