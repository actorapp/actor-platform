/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import java.util.ArrayList;

import im.actor.runtime.Log;
import im.actor.runtime.actors.ask.AskCallback;
import im.actor.runtime.actors.ask.AskIntRequest;
import im.actor.runtime.actors.ask.AskMessage;
import im.actor.runtime.actors.ask.AskResult;
import im.actor.runtime.actors.mailbox.Mailbox;
import im.actor.runtime.actors.messages.DeadLetter;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.promise.Promise;
import im.actor.runtime.promise.PromiseDispatch;
import im.actor.runtime.promise.PromiseFunc;
import im.actor.runtime.promise.PromiseResolver;

/**
 * Actor object
 */
public class Actor {

    private String path;

    private ActorContext context;
    private Mailbox mailbox;

    private ArrayList<Receiver> receivers = new ArrayList<Receiver>();
    private ArrayList<StashedMessage> stashed = new ArrayList<StashedMessage>();

    public Actor() {

    }

    /**
     * <p>INTERNAL API</p>
     * Initialization of actor
     *
     * @param path    path of actor
     * @param context context of actor
     * @param mailbox mailbox of actor
     */
    public final void initActor(String path, ActorContext context, Mailbox mailbox) {
        this.path = path;
        this.context = context;
        this.mailbox = mailbox;
    }

    /**
     * <p>INTERNAL API</p>
     * Handling of a message in Actor
     *
     * @param message message
     */
    public final void handleMessage(Object message, ActorRef sender) {
        intHandle(message, sender);
    }

    public void stash() {
        stashed.add(new StashedMessage(context.message(), context.sender()));
    }

    public void unstashAll() {
        StashedMessage[] msgs = stashed.toArray(new StashedMessage[stashed.size()]);
        stashed.clear();
        for (int i = msgs.length - 1; i >= 0; i--) {
            self().sendFirst(msgs[i].getMessage(), msgs[i].getSender());
        }
    }

    public void become(Receiver receiver) {
        receivers.add(receiver);
    }

    public void unbecome() {
        receivers.remove(receivers.size() - 1);
    }

    private void intHandle(Object message, ActorRef sender) {
        context.setSender(sender);
        context.setMessage(message);

        if (receivers.size() > 0) {
            receivers.get(receivers.size() - 1).onReceive(message);
            return;
        }

        if (message instanceof Runnable) {
            ((Runnable) message).run();
            return;
        }


        onReceive(message);
    }

    /**
     * Actor System
     *
     * @return Actor System
     */
    public final ActorSystem system() {
        return context.getSystem();
    }

    /**
     * Self actor reference
     *
     * @return self reference
     */
    public final ActorRef self() {
        return context.getSelf();
    }

//    /**
//     * Actor context
//     *
//     * @return context
//     */
//    protected final ActorContext context() {
//        return context;
//    }

    /**
     * Sender of last received message
     *
     * @return sender's ActorRef
     */
    public final ActorRef sender() {
        return context.sender();
    }

    /**
     * Actor path
     *
     * @return path
     */
    protected final String getPath() {
        return path;
    }

    /**
     * Actor mailbox
     *
     * @return mailbox
     */
    public final Mailbox getMailbox() {
        return mailbox;
    }

    /**
     * Called before first message receiving
     */
    public void preStart() {

    }

    /**
     * Receiving of message
     *
     * @param message message
     */
    public void onReceive(Object message) {
        drop(message);
    }

    /**
     * Called after actor shutdown
     */
    public void postStop() {

    }

    /**
     * finally-like method before actor death
     */
    public void finallyStop() {

    }

    /**
     * Reply message to sender of last message
     *
     * @param message reply message
     */
    public void reply(Object message) {
        if (context.sender() != null) {
            context.sender().send(message, self());
        }
    }

    /**
     * Dropping of message
     *
     * @param message message for dropping
     */
    public void drop(Object message) {
        if (system().getTraceInterface() != null) {
            system().getTraceInterface().onDrop(sender(), message, this);
        }
        reply(new DeadLetter(message));
    }

    public void halt(String message) {
        halt(message, null);
    }

    public void halt(String message, Exception e) {
        throw new ActorHalterException(message, e);
    }

    public <T extends AskResult> Promise<T> ask(final ActorRef dest, final AskMessage<T> msg) {
        return new Promise<T>(new PromiseFunc<T>() {
            @Override
            public void exec(PromiseResolver<T> executor) {
                dest.send(new AskIntRequest(msg, executor));
            }
        });
    }

    public void ask(ActorRef dest, Object message) {
        ask(dest, message, null);
    }

    public void ask(final ActorRef dest, final Object message, final AskCallback callback) {
        new Promise<Object>(new PromiseFunc<Object>() {
            @Override
            public void exec(final PromiseResolver<Object> executor) {
                become(new Receiver() {
                    @Override
                    public void onReceive(Object message) {
                        if (message instanceof PromiseDispatch) {
                            PromiseDispatch dispatch = (PromiseDispatch) message;
                            if (dispatch.getPromise() == executor.getPromise()) {
                                dispatch.run();
                            } else {
                                stash();
                            }
                        } else {
                            stash();
                        }
                    }
                });
                dest.send(new AskIntRequest(message, executor));
            }
        }).then(new Consumer<Object>() {
            @Override
            public void apply(Object o) {
                unbecome();
                unstashAll();

                if (callback != null) {
                    callback.onResult(o);
                }
            }
        }).failure(new Consumer<Exception>() {
            @Override
            public void apply(Exception e) {
                unbecome();
                unstashAll();

                if (callback != null) {
                    callback.onError(e);
                }
            }
        }).done(self());
    }
}