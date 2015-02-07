package com.droidkit.actors.typed;

import com.droidkit.actors.ActorRef;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.typed.messages.TypedRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Factory of typed actors proxies
 *
 * @author Steve Ex3NDR Korshakov (steve@actor.im)
 */
public class TypedCreator {
    public static <T> T typed(final ActorRef ref, Class<T> tClass) {
        return (T) Proxy.newProxyInstance(ref.system().getClassLoader(),
                new Class[]{tClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        ClientFuture future = null;
                        if (method.getReturnType().equals(Future.class)) {
                            future = new ClientFuture();
                        }
                        ref.send(new TypedRequest(future, method, args));
                        return future;
                    }
                });
    }
}