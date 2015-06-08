/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.network.mtp.actors;

import java.io.IOException;
import java.util.ArrayList;

import im.actor.model.droidkit.actors.Actor;
import im.actor.model.droidkit.actors.ActorCreator;
import im.actor.model.droidkit.actors.ActorRef;
import im.actor.model.droidkit.actors.ActorSelection;
import im.actor.model.droidkit.actors.ActorSystem;
import im.actor.model.droidkit.actors.Props;
import im.actor.model.log.Log;
import im.actor.model.network.mtp.MTProto;
import im.actor.model.network.mtp.entity.AuthIdInvalid;
import im.actor.model.network.mtp.entity.Container;
import im.actor.model.network.mtp.entity.MTPush;
import im.actor.model.network.mtp.entity.MTRpcResponse;
import im.actor.model.network.mtp.entity.MessageAck;
import im.actor.model.network.mtp.entity.NewSessionCreated;
import im.actor.model.network.mtp.entity.ProtoMessage;
import im.actor.model.network.mtp.entity.ProtoSerializer;
import im.actor.model.network.mtp.entity.ProtoStruct;
import im.actor.model.network.mtp.entity.RequestResend;
import im.actor.model.network.mtp.entity.SessionLost;
import im.actor.model.network.mtp.entity.UnsentMessage;
import im.actor.model.network.mtp.entity.UnsentResponse;
import im.actor.model.network.util.MTUids;

public class ReceiverActor extends Actor {

    private static final String TAG = "ProtoReceiver";

    public static ActorRef receiver(final MTProto proto) {
        return ActorSystem.system().actorOf(new ActorSelection(Props.create(ReceiverActor.class, new ActorCreator<ReceiverActor>() {
            @Override
            public ReceiverActor create() {
                return new ReceiverActor(proto);
            }
        }), proto.getActorPath() + "/receiver"));
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
        sender = SenderActor.senderActor(proto);
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

        sender.send(new SenderActor.ReadPackageFromConnection());

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
                sender.send(new SenderActor.NewSession(newSessionCreated.getMessageId()));
                proto.getCallback().onSessionCreated();
            } else if (obj instanceof Container) {
                Container container = (Container) obj;
                for (ProtoMessage m : container.getMessages()) {
                    self().send(m, sender());
                }
            } else if (obj instanceof SessionLost) {
                sender.send(new SenderActor.SessionLost());
            } else if (obj instanceof MTRpcResponse) {
                MTRpcResponse responseBox = (MTRpcResponse) obj;
                // Forget messages
                sender.send(new SenderActor.ForgetMessage(responseBox.getMessageId()));
                proto.getCallback().onRpcResponse(responseBox.getMessageId(), responseBox.getPayload());
            } else if (obj instanceof MessageAck) {
                MessageAck ack = (MessageAck) obj;

                for (long ackMsgId : ack.messagesIds) {
                    sender.send(new SenderActor.ForgetMessage(ackMsgId));
                }
            } else if (obj instanceof MTPush) {
                MTPush box = (MTPush) obj;
                proto.getCallback().onUpdate(box.getPayload());
            } else if (obj instanceof UnsentResponse) {
                UnsentResponse unsent = (UnsentResponse) obj;
                if (!receivedMessages.contains(unsent.getResponseMessageId())) {
                    disableConfirm = true;
                    sender.send(new SenderActor.SendMessage(MTUids.nextId(),
                            new RequestResend(unsent.getMessageId()).toByteArray()));
                }
            } else if (obj instanceof UnsentMessage) {
                UnsentMessage unsent = (UnsentMessage) obj;
                if (!receivedMessages.contains(unsent.getMessageId())) {
                    disableConfirm = true;
                    sender.send(new SenderActor.SendMessage(MTUids.nextId(),
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
                sender.send(new SenderActor.ConfirmMessage(message.getMessageId()));
            }
        }
    }
}
