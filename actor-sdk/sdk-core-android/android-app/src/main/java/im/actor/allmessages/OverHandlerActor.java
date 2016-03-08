package im.actor.allmessages;

import java.util.ArrayList;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.Reaction;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.storage.ListEngine;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class OverHandlerActor extends Actor {
    ListEngine<MessageEx> allMessages = messenger().getCustomConversationEngine(new Peer(PeerType.PRIVATE, 1), "favorite", MessageEx.CREATOR);

    public void onIncoming(Peer peer, ArrayList<Message> msgs) {
        ArrayList<MessageEx> msgex= new ArrayList<MessageEx>();
        for (Message m:msgs) {
            boolean containsMyLike = false;
            for (Reaction r:m.getReactions()) {
                if(r.getUids().contains(myUid())){
                    containsMyLike = true;
                    break;
                }
            }
            if(containsMyLike){
                msgex.add(new MessageEx(m, peer));
            }
        }
        allMessages.addOrUpdateItems(msgex);
    }

    public void onIncoming(Peer peer, Message msg) {
        boolean containsMyLike = false;
        for (Reaction r:msg.getReactions()) {
            if(r.getUids().contains(myUid())){
                containsMyLike = true;
                break;
            }
        }
        if(containsMyLike){
            allMessages.addOrUpdateItem(new MessageEx(msg, peer));
        }
    }

    public void onUpdate(Peer peer, Message msg) {
        boolean containsMyReaction = false;
        for (Reaction r:msg.getReactions()) {
            if(r.getUids().contains(myUid())){
                containsMyReaction = true;
                break;
            }
        }
        if(containsMyReaction){
            allMessages.addOrUpdateItem(new MessageEx(msg, peer));
        }else{
            allMessages.removeItem(msg.getEngineId());

        }

    }

    public void onDelete(Peer peer, long[] rids) {
        allMessages.removeItems(rids);
    }

    @Override
    public void onReceive(Object message) {
        if(message instanceof Incoming){
            ArrayList<Message> msgs = ((Incoming) message).getMsgs();
            if(msgs!=null){
                onIncoming(((Incoming) message).getPeer(), msgs);
            } else {
                onIncoming(((Incoming) message).getPeer(), ((Incoming) message).getMsg());
            }
        }else if(message instanceof Delete){
            onDelete(((Delete) message).getPeer(), ((Delete) message).getRids());
        }else if(message instanceof Update){
            onUpdate(((Update) message).getPeer(), ((Update) message).getMsg());
        }
    }

    public static class Incoming{
        Peer peer;
        Message msg;

        ArrayList<Message> msgs;

        public Incoming(Peer peer, Message msg) {
            this.peer = peer;
            this.msg = msg;
        }

        public Incoming(Peer peer, ArrayList<Message> msgs) {
            this.peer = peer;
            this.msgs = msgs;
        }

        public Peer getPeer() {
            return peer;
        }

        public ArrayList<Message> getMsgs() {
            return msgs;
        }

        public Message getMsg() {
            return msg;
        }
    }

    public static class Delete{
        Peer peer;
        long[] rids;

        public Delete(Peer peer, long[] rids) {
            this.peer = peer;
            this.rids = rids;
        }

        public Peer getPeer() {
            return peer;
        }

        public long[] getRids() {
            return rids;
        }
    }


    public static class Update{
        Peer peer;
        Message msg;

        public Update(Peer peer, Message msg) {
            this.peer = peer;
            this.msg = msg;
        }

        public Peer getPeer() {
            return peer;
        }

        public Message getMsg() {
            return msg;
        }
    }


}
