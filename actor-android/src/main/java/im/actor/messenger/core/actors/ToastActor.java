package im.actor.messenger.core.actors;

import android.widget.Toast;

import com.droidkit.actors.typed.TypedActor;

import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.base.TypedActorHolder;

/**
 * Created by ex3ndr on 30.11.14.
 */
public class ToastActor extends TypedActor<ToastInt> implements ToastInt {

    private static final TypedActorHolder<ToastInt> HOLDER = new TypedActorHolder<ToastInt>(ToastInt.class,
            ToastActor.class, "ui", "toast");

    public static ToastInt get() {
        return HOLDER.get();
    }

    public ToastActor() {
        super(ToastInt.class);
    }

    @Override
    public void show(String text) {
        Toast.makeText(AppContext.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLong(String text) {
        Toast.makeText(AppContext.getContext(), text, Toast.LENGTH_LONG).show();
    }
}
