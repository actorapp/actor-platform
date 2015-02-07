package com.droidkit.actors.typed;

import com.droidkit.actors.Actor;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.actors.typed.messages.TypedRequest;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class TypedActor<T> extends Actor {

    public Class<T> clazz;

    public TypedActor(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof TypedRequest) {
            final TypedRequest req = (TypedRequest) message;
            try {
                if (req.getMethod().getReturnType().equals(Future.class)) {
                    try {
                        Future future = (Future) req.getMethod().invoke(this, req.getArgs());
                        if (future instanceof ResultFuture) {
                            req.getFuture().doComplete(future.get());
                        } else if (future instanceof TypedFuture) {
                            future.addListener(new FutureCallback() {
                                @Override
                                public void onResult(Object result) {
                                    req.getFuture().doComplete(result);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    req.getFuture().doError(throwable);
                                }
                            });
                        } else {
                            // Unsupported
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        req.getFuture().doError(t);
                    }

                } else {
                    req.getMethod().invoke(this, req.getArgs());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    protected <V> Future result(V obj) {
        return new ResultFuture<V>(obj);
    }

    protected <V> TypedFuture<V> future() {
        return new TypedFuture<V>();
    }
}
