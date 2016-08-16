package im.actor.sdk.controllers;

import android.view.View;
import android.widget.TextView;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.ValueDoubleListener;
import im.actor.runtime.mvvm.ValueListener;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class BinderCompatFragment extends android.support.v4.app.Fragment {

    private final ActorBinder BINDER = new ActorBinder();

    private boolean unbindOnPause = false;

    public boolean isUnbindOnPause() {
        return unbindOnPause;
    }

    public void setUnbindOnPause(boolean unbindOnPause) {
        this.unbindOnPause = unbindOnPause;
    }

    public void bind(final TextView textView, ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T> void bind(ValueModel<T> value, ValueListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T1, T2> void bind(Value<T1> value1, Value<T2> value2, ValueDoubleListener<T1, T2> listener) {
        BINDER.bind(value1, value2, listener);
    }

    public <T> void bind(ValueModel<T> value, boolean notify, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener, notify);
    }

    public <T1, T2> ActorBinder.Binding[] bind(ValueModel<T1> value1, ValueModel<T2> value2, ValueDoubleChangedListener<T1, T2> listener) {
        return BINDER.bind(value1, value2, listener);
    }

    public void bind(final CoverAvatarView avatarView, final ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    public ActorBinder.Binding bind(final TextView textView, final UserVM user) {
        return BINDER.bind(textView, user);
    }

    public void bind(final AvatarView avatarView, final int id,
                     final Value<Avatar> avatar, final Value<String> name) {
        BINDER.bind(avatarView, id, avatar, name);
    }

    public void bindGroupTyping(final TextView textView, final View container, final View titleContainer, final Value<int[]> typing) {
        BINDER.bindGroupTyping(textView, container, titleContainer, typing);
    }

    public void bind(final TextView textView, View titleContainer, GroupVM value) {
        BINDER.bind(textView, titleContainer, value);
    }

    public void bindPrivateTyping(final TextView textView, final View container, final View titleContainer, final Value<Boolean> typing) {
        BINDER.bindPrivateTyping(textView, container, titleContainer, typing);
    }

    public ActorBinder.Binding bindOnline(final TextView textView, final UserVM user) {
        return BINDER.bindOnline(textView, user);
    }

    public void bind(final TextView textView, final View container, ValueModel<String> value) {
        BINDER.bind(textView, container, value);
    }

    public void bind(final TextView textView, final View container, ValueModel<String> value, ActorBinder.OnChangedListener callback, boolean hide, String defaultValue) {
        BINDER.bind(textView, container, value, callback, hide, defaultValue);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (unbindOnPause) {
            BINDER.unbindAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!unbindOnPause) {
            BINDER.unbindAll();
        }
    }

    protected ActorBinder getBINDER() {
        return BINDER;
    }

    public void unbind(ActorBinder.Binding b) {
        BINDER.unbind(b);
    }
}
