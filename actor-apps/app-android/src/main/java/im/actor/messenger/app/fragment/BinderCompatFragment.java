package im.actor.messenger.app.fragment;

import android.view.View;
import android.widget.TextView;

import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class BinderCompatFragment extends android.support.v4.app.Fragment {

    private final ActorBinder BINDER = new ActorBinder();

    public void bind(final TextView textView, im.actor.model.mvvm.ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        BINDER.bind(value, listener);
    }

    public void bind(final CoverAvatarView avatarView, final im.actor.model.mvvm.ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        BINDER.bind(textView, container, user);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BINDER.unbindAll();
    }
}
