package im.actor.core.modules.sequence.internal;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiDialogGroup;
import im.actor.core.api.ApiDialogShort;
import im.actor.core.api.updates.UpdateChatGroupsChanged;
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

import static im.actor.core.entity.EntityConverter.convert;

public class GetDiffCombiner {

    public static CombinedDifference buildDiff(List<Update> updates) {
        CombinedDifference res = new CombinedDifference();
        UpdateChatGroupsChanged chatGroupsChanged = null;
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
                int counter = 0;
                if (readByMe.getUnreadCounter() != null) {
                    counter = readByMe.getUnreadCounter();
                }
                res.putReadByMe(convert(readByMe.getPeer()), readByMe.getStartDate(), counter);
            } else if (u instanceof UpdateCountersChanged) {
                // Ignore
            } else if (u instanceof UpdateChatGroupsChanged) {
                chatGroupsChanged = (UpdateChatGroupsChanged) u;
            } else {
                res.getOtherUpdates().add(u);
            }
        }

        // Handling reordering of updates
        if (chatGroupsChanged != null) {
            ArrayList<ApiDialogGroup> dialogs = new ArrayList<>();
            for (ApiDialogGroup dg : chatGroupsChanged.getDialogs()) {
                ArrayList<ApiDialogShort> dialogShorts = new ArrayList<>();
                for (ApiDialogShort ds : dg.getDialogs()) {
                    CombinedDifference.ReadByMeValue val = res.getReadByMe().get(convert(ds.getPeer()));
                    if (val != null) {
                        dialogShorts.add(new ApiDialogShort(ds.getPeer(),
                                val.getCounter(), ds.getDate()));
                    } else {
                        dialogShorts.add(ds);
                    }
                }
                dialogs.add(new ApiDialogGroup(dg.getTitle(), dg.getKey(), dialogShorts));
            }
            res.getOtherUpdates().add(new UpdateChatGroupsChanged(dialogs));
        }

        return res;
    }
}