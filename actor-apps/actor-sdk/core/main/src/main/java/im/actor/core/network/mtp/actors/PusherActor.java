/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSelection;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.Log;
import im.actor.core.network.mtp.MTProto;
import im.actor.core.network.mtp.entity.Container;
import im.actor.core.network.mtp.entity.MessageAck;
import im.actor.core.network.mtp.entity.ProtoMessage;
import im.actor.core.network.mtp.entity.SessionHello;
import im.actor.core.network.util.MTUids;

public class PusherActor extends Actor {

    private static final String TAG = "ProtoSender";

    public static ActorRef senderActor(final MTProto proto) {
        return ActorSystem.system().actorOf(new ActorSelection(Props.create(PusherActor.class, new ActorCreator<PusherActor>() {
            @Override
            public PusherActor create() {
                return new PusherActor(proto);
            }
        }).changeDispatcher("network"), proto.getActorPath() + "/sender"));
    }

    private static final int ACK_THRESHOLD = 10;
    private static final int ACK_DELAY = 10 * 1000;
    private static final int MAX_WORKLOAD_SIZE = 1024;

    private boolean isEnableLog;
    private MTProto proto;
    private ActorRef manager;

    private HashMap<Long, ProtoMessage> unsentPackages;
    private HashSet<Long> confirm;

    private HashSet<Long> pendingConfirm;

    public PusherActor(MTProto proto) {
        this.proto = proto;
        this.isEnableLog = proto.isEnableLog();
        this.unsentPackages = new HashMap<Long, ProtoMessage>();
        this.pendingConfirm = new HashSet<Long>();
        this.confirm = new HashSet<Long>();
    }

    @Override
    public void preStart() {
        manager = ManagerActor.manager(proto);
    }

    @Override
    public void postStop() {
        this.unsentPackages = null;
        this.confirm = null;
        this.pendingConfirm = null;
        this.proto = null;
        this.manager = null;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof SendMessage) {

            if (isEnableLog) {
                Log.d(TAG, "Received SendMessage #" + ((SendMessage) message).mid);
            }

            SendMessage sendMessage = (SendMessage) message;
            ProtoMessage holder = new ProtoMessage(sendMessage.mid, sendMessage.message);
            unsentPackages.put(holder.getMessageId(), holder);
            doSend(holder);
        } else if (message instanceof ConnectionCreated) {
            if (isEnableLog) {
                Log.d(TAG, "Received ConnectionCreated");
            }

            // Marking all pending confirms as unsent
            confirm.addAll(pendingConfirm);
            pendingConfirm.clear();

            // Resending unsent messages
            ArrayList<ProtoMessage> toSend = new ArrayList<ProtoMessage>();
            for (ProtoMessage unsentPackage : unsentPackages.values()) {
                if (isEnableLog) {
                    Log.d(TAG, "ReSending #" + unsentPackage.getMessageId());
                }
                toSend.add(unsentPackage);
            }

            // Sending SessionHello if there is no packages to sent
            if (toSend.size() == 0) {
                if (isEnableLog) {
                    Log.d(TAG, "Sending SessionHello");
                }
                toSend.add(new ProtoMessage(MTUids.nextId(), new SessionHello().toByteArray()));
            }

            doSend(toSend);
        } else if (message instanceof SessionLost) {
            if (isEnableLog) {
                Log.d(TAG, "Sending SessionHello");
            }
            doSend(new ProtoMessage(MTUids.nextId(), new SessionHello().toByteArray()));
        } else if (message instanceof ForgetMessage) {
            if (isEnableLog) {
                Log.d(TAG, "Received ForgetMessage #" + ((ForgetMessage) message).mid);
            }
            unsentPackages.remove(((ForgetMessage) message).mid);
        } else if (message instanceof ConfirmMessage) {
            if (isEnableLog) {
                Log.d(TAG, "Confirming message #" + ((ConfirmMessage) message).mid);
            }
            confirm.add(((ConfirmMessage) message).mid);
            if (confirm.size() >= ACK_THRESHOLD) {
                self().sendOnce(new ForceAck());
            } else if (confirm.size() == 1) {
                self().sendOnce(new ForceAck(), ACK_DELAY);
            }
        } else if (message instanceof ForceAck) {
            if (confirm.size() == 0) {
                return;
            }

            MessageAck messageAck = buildAck();
            doSend(new ProtoMessage(MTUids.nextId(), messageAck.toByteArray()));
        } else if (message instanceof NewSession) {
            NewSession newSession = (NewSession) message;

            Log.w(TAG, "Received NewSessionCreated");

            // Clearing pending acks because of session die
            pendingConfirm.clear();
            confirm.clear();

            // Resending all required messages
            ArrayList<ProtoMessage> toSend = new ArrayList<ProtoMessage>();
            for (ProtoMessage unsentPackage : unsentPackages.values()) {
                if (unsentPackage.getMessageId() < newSession.getMessageId()) {
                    if (isEnableLog) {
                        Log.d(TAG, "ReSending #" + unsentPackage.getMessageId());
                    }
                    toSend.add(unsentPackage);
                }
            }

            doSend(toSend);
        } else if (message instanceof ReadPackageFromConnection) {
            // Clearing pending confirmation
            if (pendingConfirm.size() > 0) {
                pendingConfirm.clear();
            }
        }
    }

    private MessageAck buildAck() {
        long[] ids = new long[confirm.size()];
        Long[] ids2 = confirm.toArray(new Long[confirm.size()]);

        String acks = "";
        for (int i = 0; i < ids.length; i++) {
            ids[i] = ids2[i];
            if (isEnableLog) {
                if (acks.length() != 0) {
                    acks += ",";
                }
                acks += "#" + ids2[i];
            }
        }
        if (isEnableLog) {
            Log.d(TAG, "Sending acks " + acks);
        }

        pendingConfirm.addAll(confirm);
        confirm.clear();
        return new MessageAck(ids);
    }

    private void doSend(List<ProtoMessage> items) {
        if (items.size() > 0) {
            if (confirm.size() > 0) {
                if (isEnableLog) {
                    Log.d(TAG, "Sending acks in package");
                }
                items.add(0, new ProtoMessage(MTUids.nextId(), buildAck().toByteArray()));
            }
        }
        if (items.size() == 1) {
            doSend(items.get(0));
        } else if (items.size() > 1) {
            ArrayList<ProtoMessage> messages = new ArrayList<ProtoMessage>();
            int currentPayload = 0;
            for (int i = 0; i < items.size(); i++) {
                ProtoMessage message = items.get(i);
                currentPayload += message.getPayload().length;
                messages.add(message);
                if (currentPayload > MAX_WORKLOAD_SIZE) {
                    Container container = new Container(messages.toArray(new ProtoMessage[messages.size()]));
                    performSend(new ProtoMessage(MTUids.nextId(), container.toByteArray()));

                    messages.clear();
                    currentPayload = 0;
                }
            }
            if (messages.size() > 0) {
                Container container = new Container(messages.toArray(new ProtoMessage[messages.size()]));
                performSend(new ProtoMessage(MTUids.nextId(), container.toByteArray()));
            }
        }
    }

    private void doSend(ProtoMessage message) {
        if (confirm.size() > 0) {
            ArrayList<ProtoMessage> mtpMessages = new ArrayList<ProtoMessage>();
            mtpMessages.add(message);
            doSend(mtpMessages);
        } else {
            performSend(message);
        }
    }

    private void performSend(ProtoMessage message) {
        byte[] data = message.toByteArray();
        manager.send(new ManagerActor.OutMessage(data, 0, data.length));
    }

    public static class SendMessage {
        private long mid;
        private byte[] message;

        public SendMessage(long rid, byte[] message) {
            this.mid = rid;
            this.message = message;
        }
    }

    public static class ForgetMessage {
        private long mid;

        public ForgetMessage(long rid) {
            this.mid = rid;
        }
    }

    public static class ConfirmMessage {
        private long mid;

        public ConfirmMessage(long rid) {
            this.mid = rid;
        }
    }

    public static class ConnectionCreated {

    }

    public static class ReadPackageFromConnection {

    }

    public static class NewSession {
        private long messageId;

        public NewSession(long messageId) {
            this.messageId = messageId;
        }

        public long getMessageId() {
            return messageId;
        }
    }

    public static class SessionLost {

    }

    public static class ForceAck {

    }

    public static class StopActor {

    }
}
