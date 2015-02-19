package im.actor.messenger.app.base;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import com.droidkit.mvvm.ui.Binder;

import im.actor.messenger.app.binding.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueModel;
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

    @Override
    protected void onPause() {
        super.onPause();
        messenger().onAppHidden();
        BINDER.unbindAll();
    }
}
