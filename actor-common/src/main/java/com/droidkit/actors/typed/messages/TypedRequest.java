package com.droidkit.actors.typed.messages;

import com.droidkit.actors.typed.ClientFuture;

import java.lang.reflect.Method;

/**
 * Created by ex3ndr on 06.09.14.
 */
public class TypedRequest {
    private ClientFuture future;
    private Method method;
    private Object[] args;

    public TypedRequest(ClientFuture future, Method method, Object[] args) {
        this.future = future;
        this.method = method;
        this.args = args;
    }

    public ClientFuture getFuture() {
        return future;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "Typed<" + method.getName() + ">";
    }
}
