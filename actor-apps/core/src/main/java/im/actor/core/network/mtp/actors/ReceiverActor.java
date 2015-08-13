/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.network.mtp.actors;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSelection;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.Log;
import im.actor.core.network.mtp.MTProto;
import im.actor.core.network.mtp.entity.AuthIdInvalid;
import im.actor.core.network.mtp.entity.Container;
import im.actor.core.network.mtp.entity.MTPush;
import im.actor.core.network.mtp.entity.MTRpcResponse;
import im.actor.core.network.mtp.entity.MessageAck;
import im.actor.core.network.mtp.entity.NewSessionCreated;
import im.actor.core.network.mtp.entity.ProtoMessage;
import im.actor.core.network.mtp.entity.ProtoSerializer;
import im.actor.core.network.mtp.entity.ProtoStruct;
import im.actor.core.network.mtp.entity.RequestResend;
import im.actor.core.network.mtp.entity.SessionLost;
import im.actor.core.network.mtp.entity.UnsentMessage;
import im.actor.core.network.mtp.entity.UnsentResponse;
import im.actor.core.network.util.MTUids;

public class ReceiverActor extends Actor {

    private static final String TAG = "ProtoReceiver";

    public static ActorRef receiver(final MTProto proto) {
        return ActorSystem.system().actorOf(new ActorSelection(Props.create(ReceiverActor.class, new ActorCreator<ReceiverActor>() {
            @Override
            public ReceiverActor create() {
                return new ReceiverActor(proto);
            }
        }).changeDispatcher("network"), proto.getActorPath() + "/receiver"));
    }

    private static final int MAX_RECEIVED_BUFFER = 1000;

    private ActorRef sender;

    private MTProto proto;
    private ArrayList<Long> receivedMessages = new ArrayList<Long>();

    public ReceiverActor(MTProto proto) {
        this.proto = proto;
    }

    @Override
    public void preStart() {
        sender = PusherActor.senderActor(proto);
    }

    @Override
    public void postStop() {
        this.sender = null;
        this.proto = null;
        this.receivedMessages = null;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof ProtoMessage) {
            onReceive((ProtoMessage) message);
        } else {
            drop(message);
        }
    }

    private void onReceive(ProtoMessage message) {

        sender.send(new PusherActor.ReadPackageFromConnection());

        boolean disableConfirm = false;
        try {
            // Log.d(TAG, "Received message #" + message.getMessageId());

//            if (receivedMessages.contains(message.getMessageId())) {
//                Log.w(TAG, "Already received message #" + message.getMessageId() + ": ignoring");
//                return;
//            }

            if (receivedMessages.size() >= MAX_RECEIVED_BUFFER) {
                receivedMessages.remove(0);
            }
            receivedMessages.add(message.getMessageId());

            ProtoStruct obj;
            try {
                obj = ProtoSerializer.readMessagePayload(message.getPayload());
            } catch (IOException e) {
                Log.w(TAG, "Unable to parse message: ignoring");
                e.printStackTrace();
                return;
            }

            // Log.d(TAG, obj + "");

            if (obj instanceof NewSessionCreated) {
                NewSessionCreated newSessionCreated = (NewSessionCreated) obj;
                sender.send(new PusherActor.NewSession(newSessionCreated.getMessageId()));
                proto.getCallback().onSessionCreated();
            } else if (obj instanceof Container) {
                Container container = (Container) obj;
                for (ProtoMessage m : container.getMessages()) {
                    self().send(m, sender());
                }
            } else if (obj instanceof SessionLost) {
                sender.send(new PusherActor.SessionLost());
            } else if (obj instanceof MTRpcResponse) {
                MTRpcResponse responseBox = (MTRpcResponse) obj;
                // Forget messages
                sender.send(new PusherActor.ForgetMessage(responseBox.getMessageId()));
                proto.getCallback().onRpcResponse(responseBox.getMessageId(), responseBox.getPayload());
            } else if (obj instanceof MessageAck) {
                MessageAck ack = (MessageAck) obj;

                for (long ackMsgId : ack.messagesIds) {
                    sender.send(new PusherActor.ForgetMessage(ackMsgId));
                }
            } else if (obj instanceof MTPush) {
                MTPush box = (MTPush) obj;
                proto.getCallback().onUpdate(box.getPayload());
            } else if (obj instanceof UnsentResponse) {
                UnsentResponse unsent = (UnsentResponse) obj;
                if (!receivedMessages.contains(unsent.getResponseMessageId())) {
                    disableConfirm = true;
                    sender.send(new PusherActor.SendMessage(MTUids.nextId(),
                            new RequestResend(unsent.getMessageId()).toByteArray()));
                }
            } else if (obj instanceof UnsentMessage) {
                UnsentMessage unsent = (UnsentMessage) obj;
                if (!receivedMessages.contains(unsent.getMessageId())) {
                    disableConfirm = true;
                    sender.send(new PusherActor.SendMessage(MTUids.nextId(),
                            new RequestResend(unsent.getMessageId()).toByteArray()));
                }
            } else if (obj instanceof AuthIdInvalid) {
                proto.getCallback().onAuthKeyInvalidated(proto.getAuthId());
                proto.stopProto();
            } else {
                Log.w(TAG, "Unsupported package " + obj);
            }
        } catch (Exception e) {
            Log.w(TAG, "Parsing error");
        } finally {

            if (!disableConfirm) {
                sender.send(new PusherActor.ConfirmMessage(message.getMessageId()));
            }
        }
    }
}
