package im.actor.core.modules.updates.internal;

import java.util.List;

import im.actor.core.api.updates.UpdateCountersChanged;
import im.actor.core.api.updates.UpdateMessage;
import im.actor.core.api.updates.UpdateMessageRead;
import im.actor.core.api.updates.UpdateMessageReadByMe;
import im.actor.core.api.updates.UpdateMessageReceived;
import im.actor.core.api.updates.UpdateUserAboutChanged;
import im.actor.core.api.updates.UpdateUserLocalNameChanged;
import im.actor.core.api.updates.UpdateUserNameChanged;
import im.actor.core.api.updates.UpdateUserNickChanged;
import im.actor.core.network.parser.Update;

import static im.actor.core.modules.internal.messages.entity.EntityConverter.convert;

public class GetDiffCombiner {

    public static CombinedDifference buildDiff(List<Update> updates) {
        CombinedDifference res = new CombinedDifference();
        for (Update u : updates) {
            if (u instanceof UpdateMessage) {
                res.putMessage((UpdateMessage) u);
            } else if (u instanceof UpdateMessageRead) {
                UpdateMessageRead read = (UpdateMessageRead) u;
                res.putRead(convert(read.getPeer()), read.getStartDate());
            } else if (u instanceof UpdateMessageReceived) {
                UpdateMessageReceived received = (UpdateMessageReceived) u;
                res.putReceived(convert(received.getPeer()), received.getStartDate());
            } else if (u instanceof UpdateMessageReadByMe) {
                UpdateMessageReadByMe readByMe = (UpdateMessageReadByMe) u;
                res.putReadByMe(convert(readByMe.getPeer()), readByMe.getStartDate());
            } else if (u instanceof UpdateCountersChanged) {
                res.setCounters(((UpdateCountersChanged) u).getCounters());
            } else if (u instanceof UpdateUserNameChanged) {
                // Ignore
            } else if (u instanceof UpdateUserLocalNameChanged) {
                // Ignore
            } else if (u instanceof UpdateUserNickChanged) {
                // Ignore
            } else if (u instanceof UpdateUserAboutChanged) {
                // Ignore
            } else {
                res.getOtherUpdates().add(u);
            }
        }
        return res;
    }
}