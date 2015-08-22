package im.actor.runtime.generic.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ListProcessor<T> {

    @ObjectiveCName("processWithItems:withPrevious:")
    @Nullable
    Object process(@NotNull List<T> items, @Nullable Object previous);
}
