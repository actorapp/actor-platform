package im.actor.model.network.mtp.actors;

import com.droidkit.actors.*;
import im.actor.model.log.Log;
import im.actor.model.network.mtp.MTProto;
import im.actor.model.network.mtp.entity.*;
import im.actor.model.network.util.MTUids;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ex3ndr on 03.09.14.
 */
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
    public void onReceive(Object message) {
        if (message instanceof ProtoMessage) {
            onReceive((ProtoMessage) message);
        } else {
            drop(message);
        }
    }

    private void onReceive(ProtoMessage message) {
        boolean disableConfirm = false;
        try {
            Log.d(TAG, "Received message #" + message.getMessageId());

            if (receivedMessages.contains(message.getMessageId())) {
                Log.w(TAG, "Already received message #" + message.getMessageId() + ": ignoring");
                return;
            }

            if (receivedMessages.size() >= MAX_RECEIVED_BUFFER) {
                receivedMessages.remove(0);
                receivedMessages.add(message.getMessageId());
            }

            ProtoStruct obj;
            try {
                obj = ProtoSerializer.readMessagePayload(message.getPayload());
            } catch (IOException e) {
                Log.w(TAG, "Unable to parse message: ignoring");
                e.printStackTrace();
                return;
            }

            Log.d(TAG, obj + "");

            if (obj instanceof NewSessionCreated) {
                sender.send(new SenderActor.NewSession());
                proto.getCallback().onSessionCreated();
            } else if (obj instanceof Container) {
                Container container = (Container) obj;
                for (ProtoMessage m : container.getMessages()) {
                    self().send(m, sender());
                }
            } else if (obj instanceof MTRpcResponse) {
                MTRpcResponse responseBox = (MTRpcResponse) obj;
                // Forget messages
                sender.send(new SenderActor.ForgetMessage(responseBox.getMessageId()));
                proto.getCallback().onRpcResponse(responseBox.getMessageId(), responseBox.getPayload());

//                try {
//                    ProtoStruct payload = ProtoSerializer.readRpcResponsePayload(new ByteArrayInputStream(responseBox.getPayload()));
//                    Log.d(TAG, "Loaded " + payload + " from RpcResponseBox");
//                    if (payload instanceof RpcOk) {
//                        RpcOk rpcOk = (RpcOk) payload;
//                        // stateBroker.send(new RpcMessage(responseBox.getMessageId(), rpcOk.responseType, rpcOk.payload));
//
//                    } else if (payload instanceof RpcError) {
//                        RpcError rpcError = (RpcError) payload;
////                        stateBroker.send(new im.actor.api.mtp.messages
////                                .RpcError(responseBox.getMessageId(), rpcError.errorCode, rpcError.errorTag,
////                                rpcError.userMessage, rpcError.canTryAgain, rpcError.relatedData));
//                    } else {
//                        Log.w(TAG, "Unsupported RpcResponse type");
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.w(TAG, "Unable to load data from MTRpcResponse");
//                }
            } else if (obj instanceof MessageAck) {
                MessageAck ack = (MessageAck) obj;

                for (long ackMsgId : ack.messagesIds) {
                    sender.send(new SenderActor.ForgetMessage(ackMsgId));
                }
            } else if (obj instanceof MTPush) {
                MTPush box = (MTPush) obj;
                proto.getCallback().onUpdate(box.getPayload());
//                try {
//                    Update update = ProtoSerializer.readUpdate(box.getPayload());
//                    stateBroker.send(new im.actor.api.mtp.messages.Update(update.updateType, update.body));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    if (LOG != null) {
//                        LOG.w(TAG, "Unable to load data from UpdateBox");
//                    }
//                }
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
