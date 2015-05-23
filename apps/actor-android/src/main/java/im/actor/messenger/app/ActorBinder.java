package im.actor.messenger.app;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.model.entity.Avatar;
import im.actor.model.entity.GroupMember;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueDoubleChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.mvvm.ValueTripleChangedListener;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserPresence;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.users;

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

    public void bindGroupTyping(final TextView textView, final View container, final View titleContainer, final ValueModel<int[]> typing) {
        bind(typing, new ValueChangedListener<int[]>() {
            @Override
            public void onChanged(int[] val, ValueModel<int[]> valueModel) {
                if (val.length == 0) {
                    container.setVisibility(View.INVISIBLE);
                    titleContainer.setVisibility(View.VISIBLE);
                } else {
                    if (val.length == 1) {
                        textView.setText(messenger().getFormatter().formatTyping(users().get(val[0]).getName().get()));
                    } else {
                        textView.setText(messenger().getFormatter().formatTyping(val.length));
                    }
                    container.setVisibility(View.VISIBLE);
                    titleContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void bindPrivateTyping(final TextView textView, final View container, final View titleContainer,
                                  final ValueModel<Boolean> typing) {
        bind(typing, new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                if (val) {
                    textView.setText(messenger().getFormatter().formatTyping());
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
                String s = messenger().getFormatter().formatPresence(val, user.getSex());
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

    public void bind(final TextView textView, final View titleContainer, final GroupVM value) {
        bind(value.getPresence(), value.getMembers(), value.isMember(), new ValueTripleChangedListener<Integer, HashSet<GroupMember>, Boolean>() {
            @Override
            public void onChanged(Integer online, ValueModel<Integer> onlineModel,
                                  HashSet<GroupMember> members, ValueModel<HashSet<GroupMember>> membersModel, Boolean isMember, ValueModel<Boolean> isMemberModel) {
                if (isMember) {
                    titleContainer.setVisibility(View.VISIBLE);
                    if (online <= 0) {
                        SpannableStringBuilder builder = new SpannableStringBuilder(
                                messenger().getFormatter().formatGroupMembers(members.size()));
                        builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        textView.setText(builder);
                    } else {
                        SpannableStringBuilder builder = new SpannableStringBuilder(
                                messenger().getFormatter().formatGroupMembers(members.size()) + ", ");
                        builder.setSpan(new ForegroundColorSpan(0xB7ffffff), 0, builder.length(),
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        builder.append(messenger().getFormatter().formatGroupOnline(online));
                        textView.setText(builder);
                    }
                } else {
                    titleContainer.setVisibility(View.GONE);
                }
            }
        });
    }

    public void bind(final AvatarView avatarView, final int id,
                     final ValueModel<Avatar> avatar, final ValueModel<String> name) {
        bind(avatar, name, new ValueDoubleChangedListener<Avatar, String>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel, String val2, ValueModel<String> valueModel2) {
                avatarView.bind(val, val2, id, false);
            }
        });
    }

    public void bind(final CoverAvatarView avatarView, final ValueModel<Avatar> avatar) {
        bind(avatar, new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, ValueModel<Avatar> valueModel) {
                if (val != null) {
                    avatarView.bind(val);
                } else {
                    avatarView.unbind();
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
                            final ValueDoubleChangedListener<T, V> listener) {

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

    public <T, V, S> void bind(final ValueModel<T> value1, final ValueModel<V> value2, final ValueModel<S> value3,
                               final ValueTripleChangedListener<T, V, S> listener) {
        bind(value1, false, new ValueChangedListener<T>() {
            @Override
            public void onChanged(T val, ValueModel<T> valueModel) {
                listener.onChanged(val, valueModel, value2.get(), value2, value3.get(), value3);
            }
        });
        bind(value2, false, new ValueChangedListener<V>() {
            @Override
            public void onChanged(V val, ValueModel<V> valueModel) {
                listener.onChanged(value1.get(), value1, val, valueModel, value3.get(), value3);
            }
        });
        bind(value3, false, new ValueChangedListener<S>() {
            @Override
            public void onChanged(S val, ValueModel<S> valueModel) {
                listener.onChanged(value1.get(), value1, value2.get(), value2, val, valueModel);
            }
        });
        listener.onChanged(value1.get(), value1, value2.get(), value2, value3.get(), value3);
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
