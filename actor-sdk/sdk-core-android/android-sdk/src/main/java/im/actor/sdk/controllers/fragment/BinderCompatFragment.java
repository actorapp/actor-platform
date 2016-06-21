package im.actor.sdk.controllers.fragment;

import android.view.View;
import android.widget.TextView;

import im.actor.core.entity.Avatar;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

public class BinderCompatFragment extends android.support.v4.app.Fragment {

    private final ActorBinder BINDER = new ActorBinder();

    public void bind(final TextView textView, ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public <T1, T2> void bind(ValueModel<T1> value1, ValueModel<T2> value2, ValueDoubleChangedListener<T1, T2> listener) {
        BINDER.bind(value1, value2, listener);
    }

    public void bind(final CoverAvatarView avatarView, final ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    public ActorBinder.Binding bind(final TextView textView, final UserVM user) {
        return BINDER.bind(textView, user);
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
    public void onDestroyView() {
        super.onDestroyView();
        BINDER.unbindAll();
    }

    public void unbind(ActorBinder.Binding b) {
        BINDER.unbind(b);
    }
}
