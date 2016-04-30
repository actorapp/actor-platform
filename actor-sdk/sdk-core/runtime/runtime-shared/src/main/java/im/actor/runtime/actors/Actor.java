/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.actors;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.runtime.actors.dispatch.Mailbox;
import im.actor.runtime.actors.messages.DeadLetter;
import im.actor.runtime.threading.SimpleDispatcher;
import im.actor.runtime.threading.ThreadDispatcher;

/**
 * Actor object
 */
public class Actor {

    private final SimpleDispatcher dispatcher = runnable -> self().post(runnable);

    private Scheduler scheduler;

    private String path;

    private ActorContext context;
    private Mailbox mailbox;

    private ArrayList<Receiver> receivers;
    private HashMap<Integer, ArrayList<StashedMessage>> stashed;

    public Actor() {

    }

    /**
     * <p>INTERNAL API</p>
     */
    public SimpleDispatcher getDispatcher() {
        return dispatcher;
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
        stash(0);
    }

    public void stash(int index) {
        if (stashed == null) {
            stashed = new HashMap<>();
        }

        ArrayList<StashedMessage> stashedMessages = stashed.get(index);
        if (stashedMessages == null) {
            stashedMessages = new ArrayList<>();
            stashed.put(index, stashedMessages);
        }
        stashedMessages.add(new StashedMessage(context.message(), context.sender()));
    }

    public void unstashAll() {
        unstashAll(0);
    }

    public void unstashAll(int index) {
        if (stashed == null) {
            return;
        }
        ArrayList<StashedMessage> stashedMessages = stashed.get(index);
        if (stashedMessages == null || stashedMessages.size() == 0) {
            return;
        }
        for (StashedMessage stashedMessage : stashedMessages) {
            self().sendFirst(stashedMessage.getMessage(), stashedMessage.getSender());
        }
        stashedMessages.clear();
    }

    public void become(Receiver receiver) {
        if (receivers == null) {
            receivers = new ArrayList<>();
        }
        receivers.add(receiver);
    }

    public void unbecome() {
        if (receivers == null) {
            receivers = new ArrayList<>();
        }
        receivers.remove(receivers.size() - 1);
    }

    private void intHandle(Object message, ActorRef sender) {
        ThreadDispatcher.pushDispatcher(dispatcher);
        context.setSender(sender);
        context.setMessage(message);
        try {

            if (receivers != null && receivers.size() > 0) {
                receivers.get(receivers.size() - 1).onReceive(message);
                return;
            }

            if (message instanceof Runnable) {
                ((Runnable) message).run();
                return;
            }

            onReceive(message);

        } finally {
            ThreadDispatcher.popDispatcher();
            context.setSender(null);
            context.setMessage(null);
        }
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

    public void forward(ActorRef dest) {
        dest.send(context.message(), context.sender());
    }

    public void halt(String message) {
        halt(message, null);
    }

    public void halt(String message, Exception e) {
        throw new ActorHalterException(message, e);
    }

//    public <T> Promise<T> ask(final ActorRef dest, final AskMessage<T> msg) {
//        return new Promise<T>(new PromiseFunc<T>() {
//            @Override
//            public void exec(PromiseResolver<T> executor) {
//                dest.send(new AskIntRequest(msg, executor));
//            }
//        });
//    }
//
//    public void ask(ActorRef dest, Object message) {
//        ask(dest, message, null);
//    }
//
//    public void ask(final ActorRef dest, final Object message, final AskCallback callback) {
//        new Promise<>(new PromiseFunc<Object>() {
//            @Override
//            public void exec(@NonNull final PromiseResolver<Object> executor) {
//                become(new Receiver() {
//                    @Override
//                    public void onReceive(Object message) {
//                        if (message instanceof PromiseDispatch) {
//                            PromiseDispatch dispatch = (PromiseDispatch) message;
//                            if (dispatch.getPromise() == executor.getPromise()) {
//                                dispatch.run();
//                            } else {
//                                stash();
//                            }
//                        } else {
//                            stash();
//                        }
//                    }
//                });
//                dest.send(new AskIntRequest(message, executor));
//            }
//        }).then(new Consumer<Object>() {
//            @Override
//            public void apply(Object o) {
//                unbecome();
//                unstashAll();
//
//                if (callback != null) {
//                    callback.onResult(o);
//                }
//            }
//        }).failure(new Consumer<Exception>() {
//            @Override
//            public void apply(Exception e) {
//                unbecome();
//                unstashAll();
//
//                if (callback != null) {
//                    callback.onError(e);
//                }
//            }
//        }).done(self());
//    }

    public Cancellable schedule(final Object obj, long delay) {
        if (scheduler == null) {
            scheduler = new Scheduler(self());
        }
        if (obj instanceof Runnable) {
            return scheduler.schedule((Runnable) obj, delay);
        } else {
            return scheduler.schedule(() -> handleMessage(obj, self()), delay);
        }
    }
}