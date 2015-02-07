package com.droidkit.actors.messages;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class Ping {
    public static final Ping INSTANCE = new Ping();

    private Ping() {

    }

    @Override
    public String toString() {
        return "Ping";
    }
}
