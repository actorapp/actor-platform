package com.droidkit.actors.typed;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.extensions.ActorExtension;
import com.droidkit.actors.typed.messages.TypedFutureError;
import com.droidkit.actors.typed.messages.TypedFutureResult;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ex3ndr on 14.09.14.
 */
public class TypedAskExtensions implements ActorExtension {

    private HashMap<Integer, AskContainer> containers = new HashMap<Integer, AskContainer>();

    private ActorRef self;

    private AtomicInteger askId = new AtomicInteger();

    public TypedAskExtensions(ActorRef self) {
        this.self = self;
    }

    @Override
    public void preStart() {

    }

    @Override
    public boolean onReceive(Object message) {
        if (message instanceof TypedFutureResult) {
            TypedFutureResult futureResult = (TypedFutureResult) message;
            AskContainer container = containers.remove(futureResult.getId());
            if (container != null) {
                if (container.callback != null) {
                    container.callback.onResult(futureResult.getRes());
                }
            }
            return true;
        } else if (message instanceof TypedFutureError) {
            TypedFutureError futureError = (TypedFutureError) message;
            AskContainer container = containers.remove(futureError.getId());
            if (container != null) {
                if (container.callback != null) {
                    container.callback.onError(futureError.getT());
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void postStop() {

    }

    public <T> void ask(Future<T> future, FutureCallback<T> callback) {
        final int id = askId.incrementAndGet();
        future.addListener(new FutureCallback<T>() {
            @Override
            public void onResult(T result) {
                self.send(new TypedFutureResult(id, result));
            }

            @Override
            public void onError(Throwable throwable) {
                self.send(new TypedFutureError(id, throwable));
            }
        });
        containers.put(id, new AskContainer(id, future, callback));
    }

    private class AskContainer {
        private int id;
        private Future future;
        private FutureCallback callback;

        private AskContainer(int id, Future future, FutureCallback callback) {
            this.id = id;
            this.future = future;
            this.callback = callback;
        }
    }
}
