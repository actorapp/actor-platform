/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.entity;

import java.io.IOException;

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

/**
 * Created by ex3ndr on 20.05.15.
 */
public abstract class WrapperEntity<T extends BserObject> extends BserObject {

    private int recordField;
    private T wrapped;

    protected WrapperEntity(int recordField) {
        this.recordField = recordField;
    }

    protected WrapperEntity(int recordField, T wrapped) {
        this(recordField);
        this.wrapped = wrapped;
        applyWrapped(wrapped);
    }

    protected T getWrapped() {
        return wrapped;
    }

    public T toWrapped() {
        return wrapped;
    }

    protected void setWrapped(T wrapped) {
        this.wrapped = wrapped;
        applyWrapped(wrapped);
    }

    protected void applyWrapped(T wrapped) {
    }

    protected abstract T createInstance();

    @Override
    public void parse(BserValues values) throws IOException {
        byte[] rawWrapper = values.optBytes(recordField);
        if (rawWrapper != null) {
            wrapped = Bser.parse(createInstance(), rawWrapper);
            applyWrapped(wrapped);
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (wrapped != null) {
            writer.writeBytes(recordField, wrapped.toByteArray());
        }
    }
}
