package im.actor.messenger.app.binding;

import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Formatter;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserVM;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class ActorBinder {

    private ArrayList<Binding> bindings = new ArrayList<Binding>();

    public void bind(final TextView textView, ValueModel<String> value) {
        bind(value, new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, ValueModel<String> valueModel) {
                textView.setText(val);
            }
        });
    }

    public void bind(final TextView textView, final View container, final UserVM user) {
        bind(user.getPresence(), new ValueChangedListener<UserPresence>() {
            @Override
            public void onChanged(UserPresence val, ValueModel<UserPresence> valueModel) {
                String s = Formatter.formatPresence(val, user.getSex());
                if (s != null) {
                    container.setVisibility(View.VISIBLE);
                    textView.setText(s);
                } else {
                    container.setVisibility(View.GONE);
                    textView.setText("");
                }
            }
        });
    }

    public void bind(final AvatarView avatarView, final ValueModel<Avatar> avatar) {
        bind(avatar, new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                if (val != null) {
                    avatarView.bindAvatar(0, val);
                } else {
                    avatarView.unbind();
                }
            }
        });
    }

    public void bind(final CoverAvatarView avatarView, final ValueModel<Avatar> avatar) {
        bind(avatar, new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                if (val != null) {
                    avatarView.request(val);
                } else {
                    avatarView.clear();
                }
            }
        });
    }

    public <T> void bind(ValueModel<T> value, ValueChangedListener<T> listener) {
        value.subscribe(listener);
        bindings.add(new Binding(value, listener));
    }

    public void unbindAll() {
        for (Binding b : bindings) {
            b.unbind();
        }
        bindings.clear();
    }

    private class Binding {
        private ValueModel model;
        private ValueChangedListener listener;

        private Binding(ValueModel model, ValueChangedListener listener) {
            this.model = model;
            this.listener = listener;
        }

        public void unbind() {
            model.unsubscribe(listener);
        }
    }
}
