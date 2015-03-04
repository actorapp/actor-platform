package im.actor.messenger.app;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.messenger.R;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Formatter;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.GroupMember;
import im.actor.model.mvvm.DoubleValueChangedListener;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupTypingVM;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserTypingVM;
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

    public void bind(final TextView textView, final View container, final View titleContainer, final GroupTypingVM typing) {
        bind(typing.getActive(), new ValueChangedListener<int[]>() {
            @Override
            public void onChanged(int[] val, ValueModel<int[]> valueModel) {
                if (val.length == 0) {
                    container.setVisibility(View.INVISIBLE);
                    titleContainer.setVisibility(View.VISIBLE);
                } else {
                    textView.setText(Formatter.formatTyping(val));
                    container.setVisibility(View.VISIBLE);
                    titleContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void bind(final TextView textView, final View container, final View titleContainer,
                     final UserTypingVM typing) {
        bind(typing.getTyping(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                if (val) {
                    textView.setText(R.string.typing_private);
                    container.setVisibility(View.VISIBLE);
                    titleContainer.setVisibility(View.INVISIBLE);
                } else {
                    container.setVisibility(View.INVISIBLE);
                    titleContainer.setVisibility(View.VISIBLE);
                }
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

    public void bind(final TextView textView, final GroupVM value) {
        bind(value.getPresence(), value.getMembers(), new DoubleValueChangedListener<Integer, HashSet<GroupMember>>() {
            @Override
            public void onChanged(Integer online,
                                  ValueModel<Integer> onlineModel,
                                  HashSet<GroupMember> members,
                                  ValueModel<HashSet<GroupMember>> membersModel) {
                if (online <= 0) {
                    SpannableStringBuilder builder = new SpannableStringBuilder(
                            AppContext.getContext().getString(R.string.chat_group_members)
                                    .replace("{0}", members.size() + ""));
                    builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    textView.setText(builder);
                } else {
                    SpannableStringBuilder builder = new SpannableStringBuilder(
                            AppContext.getContext().getString(R.string.chat_group_members)
                                    .replace("{0}", members.size() + "") + ", ");
                    builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    builder.append(AppContext.getContext()
                            .getString(R.string.chat_group_members_online).replace("{0}", online + ""));
                    textView.setText(builder);
                }
            }
        });
    }

    public void bind(final AvatarView avatarView, final int id, final float size,
                     final ValueModel<Avatar> avatar, final ValueModel<String> name) {
        bind(avatar, name, new DoubleValueChangedListener<Avatar, String>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel, String val2, ValueModel<String> valueModel2) {
                avatarView.setEmptyDrawable(new AvatarDrawable(val2, id, size, AppContext.getContext()));
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

    public <T> void bind(ValueModel<T> value, boolean notify, ValueChangedListener<T> listener) {
        value.subscribe(listener, notify);
        bindings.add(new Binding(value, listener));
    }

    public <T, V> void bind(final ValueModel<T> value1, final ValueModel<V> value2,
                            final DoubleValueChangedListener<T, V> listener) {

        bind(value1, false, new ValueChangedListener<T>() {
            @Override
            public void onChanged(T val, ValueModel<T> valueModel) {
                listener.onChanged(val, valueModel, value2.get(), value2);
            }
        });
        bind(value2, false, new ValueChangedListener<V>() {
            @Override
            public void onChanged(V val, ValueModel<V> valueModel) {
                listener.onChanged(value1.get(), value1, val, valueModel);
            }
        });
        listener.onChanged(value1.get(), value1, value2.get(), value2);
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
