package im.actor.messenger.app.binding;

import android.widget.TextView;

import java.util.ArrayList;

import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.model.entity.Avatar;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;

/**
 * Created by ex3ndr on 19.02.15.
 */
public class ActorBinder {

    private ArrayList<Binding> bindings = new ArrayList<Binding>();

    public void bind(final TextView textView, ValueModel<String> value) {
        ValueChangedListener<String> listener = new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, ValueModel<String> valueModel) {
                textView.setText(val);
            }
        };
        value.subscribe(listener);
        bindings.add(new Binding(value, listener));
    }

    public void bind(final AvatarView avatarView, final ValueModel<Avatar> avatar) {
        ValueChangedListener<Avatar> listener = new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                if (val != null) {
                    avatarView.bindAvatar(0, val);
                } else {
                    avatarView.unbind();
                }
            }
        };
        avatar.subscribe(listener);
        bindings.add(new Binding(avatar, listener));
    }

    public void bind(final CoverAvatarView avatarView, final ValueModel<Avatar> avatar) {
        ValueChangedListener<Avatar> listener = new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                if (val != null) {
                    avatarView.request(val);
                } else {
                    avatarView.clear();
                }
            }
        };
        avatar.subscribe(listener);
        bindings.add(new Binding(avatar, listener));
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
