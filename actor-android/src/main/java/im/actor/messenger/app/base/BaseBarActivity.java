package im.actor.messenger.app.base;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import im.actor.messenger.app.binding.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupTypingVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserTypingVM;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.core.Core.messenger;

/**
 * Created by ex3ndr on 29.12.14.
 */
public class BaseBarActivity extends ActionBarActivity {
    private final ActorBinder BINDER = new ActorBinder();

    @Override
    protected void onResume() {
        super.onResume();
        messenger().onAppVisible();
    }

    public void bind(final TextView textView, ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public void bind(final AvatarView avatarView, final ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        BINDER.bind(textView, container, user);
    }

    public void bind(final TextView textView, GroupVM value) {
        BINDER.bind(textView, value);
    }

    public void bind(final TextView textView, final View container, final View titleContainer, final UserTypingVM typing) {
        BINDER.bind(textView, container, titleContainer, typing);
    }

    public void bind(final TextView textView, final View container, final View titleContainer, final GroupTypingVM typing) {
        BINDER.bind(textView, container, titleContainer, typing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        messenger().onAppHidden();
        BINDER.unbindAll();
    }
}
