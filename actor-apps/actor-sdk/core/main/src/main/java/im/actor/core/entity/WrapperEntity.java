/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

/**
 * Created by ex3ndr on 20.05.15.
 */
public abstract class WrapperEntity<T extends BserObject> extends BserObject {

    private int recordField;
    @SuppressWarnings("NullableProblems")
    @NotNull
    private T wrapped;

    protected WrapperEntity(int recordField, @NotNull byte[] data) throws IOException {
        this.recordField = recordField;
        this.load(data);

        //noinspection ConstantConditions
        if (wrapped == null) {
            throw new IOException("Unable to deserialize wrapped object");
        }
    }

    protected WrapperEntity(int recordField, @NotNull T wrapped) {
        this.recordField = recordField;
        this.wrapped = wrapped;
        applyWrapped(wrapped);
    }

    protected WrapperEntity(int recordField) {
        this.recordField = recordField;
    }

    @NotNull
    protected T getWrapped() {
        return wrapped;
    }

    @NotNull
    public T toWrapped() {
        return wrapped;
    }

    protected void setWrapped(@NotNull T wrapped) {
        this.wrapped = wrapped;
        applyWrapped(wrapped);
    }

    protected void applyWrapped(@NotNull T wrapped) {
    }

    @NotNull
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
        writer.writeBytes(recordField, wrapped.toByteArray());
    }
}
