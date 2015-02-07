package com.droidkit.actors.extensions;

import com.droidkit.actors.ActorRef;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class CallbackExtension implements ActorExtension {

    private HashMap<Object, Object> proxyMap = new HashMap<Object, Object>();

    private ActorRef self;

    public CallbackExtension(ActorRef self) {
        this.self = self;
    }

    @Override
    public void preStart() {

    }

    @Override
    public boolean onReceive(Object message) {
        if (message instanceof PerformCallback) {
            PerformCallback p = (PerformCallback) message;
            if (!proxyMap.containsKey(p.dest)) {
                return true;
            }
            try {
                p.method.invoke(p.dest, p.args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public <T> T proxy(final T src, Class<T> tClass) {
        if (proxyMap.containsKey(src)) {
            return (T) proxyMap.get(src);
        }
        Object res = Proxy.newProxyInstance(self.system().getClassLoader(), new Class[]{tClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                self.send(new PerformCallback(src, method, args));
                return null;
            }
        });
        proxyMap.put(src, res);
        return (T) res;
    }

    @Override
    public void postStop() {
        proxyMap.clear();
    }

    private static class PerformCallback {
        private Object dest;
        private Method method;
        private Object[] args;

        private PerformCallback(Object dest, Method method, Object[] args) {
            this.dest = dest;
            this.method = method;
            this.args = args;
        }
    }
}
