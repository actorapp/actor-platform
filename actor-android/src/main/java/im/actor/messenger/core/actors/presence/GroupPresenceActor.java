package im.actor.messenger.core.actors.presence;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;

import im.actor.api.scheme.GroupOutPeer;
import im.actor.messenger.core.actors.api.NewSessionCreated;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.GroupModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static im.actor.messenger.core.Core.requests;
import static im.actor.messenger.storage.KeyValueEngines.groups;

/**
 * Created by ex3ndr on 20.10.14.
 */
public class GroupPresenceActor extends Actor {

    public static ActorRef groupPresence() {
        return ActorSystem.system().actorOf(GroupPresenceActor.class, "groups_presence");
    }

    private static final HashSet<Integer> presence = new HashSet<Integer>();

    @Override
    public void onReceive(Object message) {
        if (message instanceof ConversationOpen) {
            ConversationOpen conv = (ConversationOpen) message;
            if (conv.getType() != DialogType.TYPE_GROUP) {
                return;
            }
            if (presence.contains(conv.getId())) {
                return;
            }
            GroupModel group = groups().get(conv.getId());
            if (group == null) {
                return;
            }
            presence.add(conv.getId());
            List<GroupOutPeer> peers = new ArrayList<GroupOutPeer>();
            peers.add(new GroupOutPeer(group.getChatId(), group.getAccessHash()));
            requests().subscribeToGroupOnline(peers);
        } else if (message instanceof NewSessionCreated) {
            List<GroupOutPeer> peers = new ArrayList<GroupOutPeer>();
            for (Integer chatId : presence) {
                GroupModel group = groups().get(chatId);
                if (group == null) {
                    continue;
                }
                peers.add(new GroupOutPeer(group.getChatId(), group.getAccessHash()));
            }
            if (peers.size() > 0) {
                requests().subscribeToGroupOnline(peers);
            }
        } else if (message instanceof OnGroupOnline) {
            GroupModel group = groups().get(((OnGroupOnline) message).chatId);
            if (group != null) {
                group.updateOnline(((OnGroupOnline) message).count);
            }
        }
    }

    public static class ConversationOpen {
        private int type;
        private int id;

        public ConversationOpen(int type, int id) {
            this.type = type;
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }
    }


    public static class OnGroupOnline {
        private int chatId;
        private int count;

        public OnGroupOnline(int chatId, int count) {
            this.chatId = chatId;
            this.count = count;
        }
    }

}
