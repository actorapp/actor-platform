package im.actor.sdk.controllers.fragment;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.core.entity.Avatar;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPresence;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.ValueTripleChangedListener;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ActorBinder {

    private ArrayList<Binding> bindings = new ArrayList<Binding>();

    public void bind(final TextView textView, Value<String> value) {
        bind(value, new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, Value<String> Value) {
                textView.setText(val);
            }
        });

    }

    public void bindGlobalCounter(ValueChangedListener<Integer> callback) {
        callback.onChanged(messenger().getGlobalState().getGlobalCounter().get(), messenger().getGlobalState().getGlobalCounter());
        bind(messenger().getGlobalState().getGlobalCounter(), callback);
    }

    public void bindGroupTyping(final TextView textView, final View container, final View titleContainer, final Value<int[]> typing) {
        bind(typing, new ValueChangedListener<int[]>() {
            @Override
            public void onChanged(int[] val, Value<int[]> Value) {
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
                                  final Value<Boolean> typing) {
        bind(typing, new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, Value<Boolean> Value) {
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

    public void bind(final TextView textView, final View container, final Value<String> value) {
        bind(textView, container, value, null, true, "");
    }

    public void bind(final TextView textView, final View container, final Value<String> value, final OnChangedListener callback, final boolean hide, final String defaultValue) {
        bind(value, new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, Value<String> Value) {

                if (val != null) {
                    if (hide) {
                        container.setVisibility(View.VISIBLE);
                    }
                    textView.setText(val);
                } else {
                    if (hide) {
                        container.setVisibility(View.GONE);
                    }
                    textView.setText(defaultValue);
                }
                if (callback != null) {
                    callback.onChanged(val);
                }
            }
        });
    }

    public Binding bind(final TextView textView, final UserVM user) {
        return bind(user.getPresence(), new ValueChangedListener<UserPresence>() {
            @Override
            public void onChanged(UserPresence val, Value<UserPresence> Value) {
                String s = messenger().getFormatter().formatPresence(val, user.getSex());
                if (s == null) {
                    s = "";
                }

                if (user.isBot()) {
                    s = textView.getContext().getString(R.string.members_adapter_bot_online_status);
                }
                textView.setText(s);
            }
        });
    }

    public Binding bindOnline(final TextView textView, final UserVM user) {
        return bind(user.getPresence(), new ValueChangedListener<UserPresence>() {
            @Override
            public void onChanged(UserPresence val, Value<UserPresence> Value) {
                String s = messenger().getFormatter().formatPresence(val, user.getSex());
                if (s == null) {
                    s = "";
                }

                if (val.getState().equals(UserPresence.State.ONLINE)) {
                    textView.setTextColor(ActorSDK.sharedActor().style.getGroupOnlineColor());
                    s = "\u25CF".concat(s);
                } else {
                    textView.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
                }
                if (user.isBot()) {
                    s = textView.getContext().getString(R.string.members_adapter_bot_online_status);
                }
                textView.setText(s);
            }
        });
    }


    public Binding bind(final View v, ValueModel<UserPresence> up) {
        return bind(up, new ValueChangedListener<UserPresence>() {

            @Override
            public void onChanged(UserPresence val, Value<UserPresence> valueModel) {
                if (val.getState().equals(UserPresence.State.ONLINE)) {
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.GONE);
                }
            }
        });
    }

    public Binding bind(final OnChangedListener<Boolean> callback, ValueModel<UserPresence> up) {
        return bind(up, new ValueChangedListener<UserPresence>() {

            @Override
            public void onChanged(UserPresence val, Value<UserPresence> valueModel) {
                callback.onChanged(val.getState().equals(UserPresence.State.ONLINE));
            }
        });
    }

    public void bind(final TextView textView, final View titleContainer, final GroupVM value) {
        bind(value.getPresence(), value.getMembers(), value.isMember(), new ValueTripleChangedListener<Integer, HashSet<GroupMember>, Boolean>() {
            @Override
            public void onChanged(Integer online, Value<Integer> onlineModel,
                                  HashSet<GroupMember> members, Value<HashSet<GroupMember>> membersModel, Boolean isMember, Value<Boolean> isMemberModel) {
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
                     final Value<Avatar> avatar, final Value<String> name) {
        bind(avatar, name, new ValueDoubleChangedListener<Avatar, String>() {
            @Override
            public void onChanged(Avatar val, Value<Avatar> Value, String val2, Value<String> Value2) {
                avatarView.bind(val, val2, id);
            }
        });
    }

    public void bind(final CoverAvatarView avatarView, final Value<Avatar> avatar) {
        bind(avatar, new ValueChangedListener<Avatar>() {
            @Override
            public void onChanged(Avatar val, Value<Avatar> Value) {
                if (val != null) {
                    avatarView.bind(val);
                } else {
                    avatarView.unbind();
                    avatarView.setImageURI(null);
                }
            }
        });
    }

    public <T> Binding bind(Value<T> value, ValueChangedListener<T> listener) {
        value.subscribe(listener);
        Binding b = new Binding(value, listener);
        bindings.add(b);
        return b;
    }

    public <T> Binding bind(Value<T> value, ValueChangedListener<T> listener, boolean notify) {
        value.subscribe(listener, notify);
        Binding b = new Binding(value, listener);
        bindings.add(b);
        return b;
    }

    public <T> void bind(Value<T> value, boolean notify, ValueChangedListener<T> listener) {
        value.subscribe(listener, notify);
        bindings.add(new Binding(value, listener));
    }

    public <T, V> void bind(final Value<T> value1, final Value<V> value2,
                            final ValueDoubleChangedListener<T, V> listener) {

        bind(value1, false, new ValueChangedListener<T>() {
            @Override
            public void onChanged(T val, Value<T> Value) {
                listener.onChanged(val, Value, value2.get(), value2);
            }
        });
        bind(value2, false, new ValueChangedListener<V>() {
            @Override
            public void onChanged(V val, Value<V> Value) {
                listener.onChanged(value1.get(), value1, val, Value);
            }
        });
        listener.onChanged(value1.get(), value1, value2.get(), value2);
    }

    public <T, V, S> void bind(final Value<T> value1, final Value<V> value2, final Value<S> value3,
                               final ValueTripleChangedListener<T, V, S> listener) {
        bind(value1, false, new ValueChangedListener<T>() {
            @Override
            public void onChanged(T val, Value<T> Value) {
                listener.onChanged(val, Value, value2.get(), value2, value3.get(), value3);
            }
        });
        bind(value2, false, new ValueChangedListener<V>() {
            @Override
            public void onChanged(V val, Value<V> Value) {
                listener.onChanged(value1.get(), value1, val, Value, value3.get(), value3);
            }
        });
        bind(value3, false, new ValueChangedListener<S>() {
            @Override
            public void onChanged(S val, Value<S> Value) {
                listener.onChanged(value1.get(), value1, value2.get(), value2, val, Value);
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

    public interface OnChangedListener<T> {
        void onChanged(T val);
    }

    public void unbind(Binding b) {
        b.unbind();
        bindings.remove(b);
    }

    public class Binding {
        private Value model;
        private ValueChangedListener listener;

        private Binding(Value model, ValueChangedListener listener) {
            this.model = model;
            this.listener = listener;
        }

        public void unbind() {
            model.unsubscribe(listener);
        }
    }
}
