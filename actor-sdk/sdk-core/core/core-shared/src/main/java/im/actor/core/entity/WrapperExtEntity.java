package im.actor.core.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public abstract class WrapperExtEntity<E extends BserObject, T extends BserObject> extends WrapperEntity<T> {

    private final int extRecordField;

    @Nullable
    private E wrappedExt;

    protected WrapperExtEntity(int recordField, int extRecordField, @NotNull byte[] data) throws IOException {
        super(recordField);
        this.extRecordField = extRecordField;
        this.load(data);
    }

    protected WrapperExtEntity(int recordField, int extRecordField, @NotNull T wrapped, @Nullable E ext) {
        super(recordField);
        this.extRecordField = extRecordField;
        this.wrappedExt = ext;
        setWrapped(wrapped); // Automatically called applyWrapped(T,E)
    }

    protected WrapperExtEntity(int recordField, int extRecordField) {
        super(recordField);
        this.extRecordField = extRecordField;
    }

    @Nullable
    protected E getWrappedExt() {
        return wrappedExt;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        byte[] rawExt = values.optBytes(extRecordField);
        if (rawExt != null) {
            wrappedExt = Bser.parse(createExtInstance(), rawExt);
        }
        super.parse(values);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (wrappedExt != null) {
            writer.writeObject(extRecordField, wrappedExt);
        }
        super.serialize(writer);
    }

    protected void applyWrapped(@NotNull T wrapped, @Nullable E ext) {

    }

    @Override
    protected void applyWrapped(@NotNull T wrapped) {
        applyWrapped(wrapped, wrappedExt);
    }

    protected abstract E createExtInstance();
}
