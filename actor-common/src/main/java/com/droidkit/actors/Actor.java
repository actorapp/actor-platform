package com.droidkit.actors;

import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.extensions.ActorExtension;
import com.droidkit.actors.extensions.CallbackExtension;
import com.droidkit.actors.extensions.RunnableExtension;
import com.droidkit.actors.mailbox.Mailbox;
import com.droidkit.actors.messages.DeadLetter;
import com.droidkit.actors.tasks.ActorAskImpl;
import com.droidkit.actors.tasks.AskCallback;
import com.droidkit.actors.tasks.AskFuture;
import com.droidkit.actors.typed.TypedAskExtensions;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Actor object
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class Actor {

    private UUID uuid;
    private String path;

    private ActorContext context;
    private Mailbox mailbox;

    private ActorAskImpl askPattern;
    private TypedAskExtensions typedAsk;
    private CallbackExtension callbackExtension;
    private ArrayList<ActorExtension> extensions = new ArrayList<ActorExtension>();

    public Actor() {

    }

    /**
     * <p>INTERNAL API</p>
     * Initialization of actor
     *
     * @param uuid    uuid of actor
     * @param path    path of actor
     * @param context context of actor
     * @param mailbox mailbox of actor
     */
    public final void initActor(UUID uuid, String path, ActorContext context, Mailbox mailbox) {
        this.uuid = uuid;
        this.path = path;
        this.context = context;
        this.mailbox = mailbox;
        this.askPattern = new ActorAskImpl(self());
        this.typedAsk = new TypedAskExtensions(self());
        this.callbackExtension = new CallbackExtension(self());
        this.extensions.add(askPattern);
        this.extensions.add(typedAsk);
        this.extensions.add(callbackExtension);
        this.extensions.add(new RunnableExtension());
    }

    /**
     * Current actor extensions
     *
     * @return extensions list
     */
    public ArrayList<ActorExtension> getExtensions() {
        return extensions;
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

    /**
     * Actor context
     *
     * @return context
     */
    protected final ActorContext context() {
        return context;
    }

    /**
     * Sender of last received message
     *
     * @return sender's ActorRef
     */
    public final ActorRef sender() {
        return context.sender();
    }

    /**
     * Actor UUID
     *
     * @return uuid
     */
    protected final UUID getUuid() {
        return uuid;
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

    /**
     * Combine multiple asks to single one
     *
     * @param futures futures from ask
     * @return future
     */
    public AskFuture combine(AskFuture... futures) {
        return askPattern.combine(futures);
    }

    /**
     * Combine multiple asks to single one
     *
     * @param callback asks callback
     * @param futures  futures from ask
     * @return future
     */
    public AskFuture combine(AskCallback<Object[]> callback, AskFuture... futures) {
        AskFuture future = combine(futures);
        future.addListener(callback);
        return future;
    }

    /**
     * Ask TaskActor for result
     *
     * @param selection ActorSelection of task
     * @return Future
     */
    public AskFuture ask(ActorSelection selection) {
        return askPattern.ask(system().actorOf(selection), 0, null);
    }

    /**
     * Ask TaskActor for result
     *
     * @param selection ActorSelection of task
     * @param timeout   timeout of task
     * @return Future
     */
    public AskFuture ask(ActorSelection selection, long timeout) {
        return askPattern.ask(system().actorOf(selection), timeout, null);
    }

    /**
     * Ask TaskActor for result
     *
     * @param selection ActorSelection of task
     * @param callback  callback for ask
     * @return Future
     */
    public <T> AskFuture<T> ask(ActorSelection selection, AskCallback<T> callback) {
        return askPattern.ask(system().actorOf(selection), 0, callback);
    }

    /**
     * Ask TaskActor for result
     *
     * @param selection ActorSelection of task
     * @param timeout   timeout of task
     * @param callback  callback for ask
     * @return Future
     */
    public <T> AskFuture<T> ask(ActorSelection selection, long timeout, AskCallback<T> callback) {
        return askPattern.ask(system().actorOf(selection), timeout, callback);
    }

    /**
     * Ask TaskActor for result
     *
     * @param ref ActorRef of task
     * @return Future
     */
    public AskFuture ask(ActorRef ref) {
        return askPattern.ask(ref, 0, null);
    }

    /**
     * Ask TaskActor for result
     *
     * @param ref     ActorRef of task
     * @param timeout timeout of task
     * @return Future
     */
    public AskFuture ask(ActorRef ref, long timeout) {
        return askPattern.ask(ref, timeout, null);
    }

    /**
     * Ask TaskActor for result
     *
     * @param ref      ActorRef of task
     * @param callback callback for ask
     * @return Future
     */
    public AskFuture ask(ActorRef ref, AskCallback callback) {
        return askPattern.ask(ref, 0, callback);
    }

    /**
     * Ask TaskActor for result
     *
     * @param ref      ActorRef of task
     * @param timeout  timeout of task
     * @param callback callback for ask
     * @return Future
     */
    public AskFuture ask(ActorRef ref, long timeout, AskCallback callback) {
        return askPattern.ask(ref, timeout, callback);
    }

    /**
     * Ask TypedActor future method for result
     *
     * @param future   Future of ask
     * @param callback callback for ask
     * @param <T>      type of result
     */
    public <T> void ask(Future<T> future, FutureCallback<T> callback) {
        typedAsk.ask(future, callback);
    }

    /**
     * Proxy callback interface for invoking methods as actor messages
     *
     * @param src    sourceCallback
     * @param tClass callback class
     * @param <T>    type of callback
     * @return proxy callback
     */
    public <T> T proxy(final T src, Class<T> tClass) {
        return callbackExtension.proxy(src, tClass);
    }
}