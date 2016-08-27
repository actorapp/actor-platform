package im.actor.sdk.controllers.group.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPhone;
import im.actor.runtime.actors.messages.*;
import im.actor.runtime.actors.messages.Void;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.R;
import im.actor.sdk.controllers.ActorBinder;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.util.AlertListBuilder;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.core.entity.GroupMember;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MembersAdapter extends HolderAdapter<GroupMember> {

    public static final int LOAD_GAP = 10;
    private static final int LIMIT = 20;
    private ArrayList<GroupMember> members = new ArrayList<GroupMember>();
    private ActorBinder BINDER = new ActorBinder();
    private boolean loadInProgress = false;
    private boolean loaddedToEnd = false;
    private LoadedCallback callback;

    public MembersAdapter(Context context, int groupId) {
        super(context);
        this.groupId = groupId;
    }

    public void setMembers(Collection<GroupMember> members) {
        setMembers(members, true, true);
    }

    public void setMembers(Collection<GroupMember> members, boolean clear, boolean sort) {
        if (clear) {
            this.members.clear();
        }
        if (sort) {
            GroupMember[] membersArray = members.toArray(new GroupMember[members.size()]);
            Arrays.sort(membersArray, (a, b) -> {
                if (a.isAdministrator() && !b.isAdministrator()) {
                    return -1;
                }
                if (b.isAdministrator() && !a.isAdministrator()) {
                    return 1;
                }
                String an = users().get(a.getUid()).getName().get();
                String bn = users().get(b.getUid()).getName().get();
                return an.compareTo(bn);
            });
            this.members.addAll(Arrays.asList(membersArray));
        } else {
            this.members.addAll(members);
        }
        notifyDataSetChanged();
    }

    @Override
    protected void onBindViewHolder(ViewHolder<GroupMember> holder, GroupMember obj, int position, Context context) {
        super.onBindViewHolder(holder, obj, position, context);
        if (position >= getCount() - LOAD_GAP) {
            loadMore();
        }

    }

    private int groupId;
    private boolean isInitiallyLoaded;
    private byte[] nextMembers;
    private ArrayList<Integer> rawMembers = new ArrayList<>();

    public void initLoad(LoadedCallback callback) {
        this.callback = callback;
        if (!isInitiallyLoaded) {
            loadMore();
        }
    }

    public interface LoadedCallback {
        void onLoaded();

        void onLoadedToEnd();
    }

    private void loadMore() {
        if (!loadInProgress && !loaddedToEnd) {
            loadInProgress = true;
            messenger().loadMembers(groupId, LIMIT, nextMembers).then(groupMembersSlice -> {
                if (!isInitiallyLoaded) {
                    isInitiallyLoaded = true;
                    if (callback != null) {
                        callback.onLoaded();
                    }
                }
                nextMembers = groupMembersSlice.getNext();
                loaddedToEnd = nextMembers == null;
                if (loaddedToEnd && callback != null) {
                    callback.onLoadedToEnd();
                }
                loadInProgress = false;
                setMembers(groupMembersSlice.getMembers(), false, false);
            });
        }
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public GroupMember getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return members.get(position).getUid();
    }

    @Override
    protected ViewHolder<GroupMember> createHolder(GroupMember obj) {
        return new GroupViewHolder();
    }

    private class GroupViewHolder extends ViewHolder<GroupMember> {

        private TextView userName;
        private View admin;
        private AvatarView avatarView;
        private TextView online;
        private ActorBinder.Binding onlineBinding;
        private UserVM user;

        @Override
        public View init(GroupMember data, ViewGroup viewGroup, Context context) {
            View res = ((Activity) context).getLayoutInflater().inflate(R.layout.fragment_group_item, viewGroup, false);
            userName = (TextView) res.findViewById(R.id.name);
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            avatarView.init(Screen.dp(42), 18);
            admin = res.findViewById(R.id.adminFlag);
            online = (TextView) res.findViewById(R.id.online);
            ((TextView) admin).setTextColor(ActorSDK.sharedActor().style.getGroupAdminColor());
            ((TextView) res.findViewById(R.id.name)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
            // res.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
            return res;
        }

        @Override
        public void bind(GroupMember data, int position, Context context) {
            boolean needRebind = user == null || data.getUid() != user.getId();
            user = users().get(data.getUid());
            ActorSDK.sharedActor().getMessenger().onUserVisible(data.getUid());
            onlineBinding = BINDER.bindOnline(online, user);

            if (needRebind) {
                avatarView.bind(user);
            }

            userName.setText(user.getName().get());

            if (data.isAdministrator()) {
                admin.setVisibility(View.VISIBLE);
            } else {
                admin.setVisibility(View.GONE);
            }
        }

        @Override
        public void unbind(boolean full) {
            if (full) {
                avatarView.unbind();
            }
            if (onlineBinding != null) {
                BINDER.unbind(onlineBinding);
            }
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        BINDER.unbindAll();
    }

    public void onMemberClick(GroupVM groupVM, UserVM userVM, boolean isAdministrator, boolean isInvitedByMe, BaseActivity activity) {
        AlertListBuilder alertListBuilder = new AlertListBuilder();
        final ArrayList<UserPhone> phones = userVM.getPhones().get();
        alertListBuilder.addItem(activity.getString(R.string.group_context_message).replace("{0}", userVM.getName().get()), () -> activity.startActivity(Intents.openPrivateDialog(userVM.getId(), true, activity)));
        if (phones.size() != 0) {
            alertListBuilder.addItem(activity.getString(R.string.group_context_call).replace("{0}", userVM.getName().get()), () -> {
                if (phones.size() == 1) {
                    activity.startActivity(Intents.call(phones.get(0).getPhone()));
                } else {
                    CharSequence[] sequences = new CharSequence[phones.size()];
                    for (int i = 0; i < sequences.length; i++) {
                        try {
                            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + phones.get(i).getPhone(), "us");
                            sequences[i] = phones.get(i).getTitle() + ": " + PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                        } catch (NumberParseException e) {
                            e.printStackTrace();
                            sequences[i] = phones.get(i).getTitle() + ": +" + phones.get(i).getPhone();
                        }
                    }
                    new AlertDialog.Builder(activity)
                            .setItems(sequences, (dialog1, which1) -> {
                                activity.startActivity(Intents.call(phones.get(which1).getPhone()));
                            })
                            .show()
                            .setCanceledOnTouchOutside(true);
                }
            });
        }
        alertListBuilder.addItem(activity.getString(R.string.group_context_view).replace("{0}", userVM.getName().get()), () -> ActorSDKLauncher.startProfileActivity(activity, userVM.getId()));
        if (groupVM.getIsCanKickAnyone().get() || (groupVM.getIsCanKickInvited().get() && isInvitedByMe)) {
            alertListBuilder.addItem(activity.getString(R.string.group_context_remove).replace("{0}", userVM.getName().get()), () -> {
                new AlertDialog.Builder(activity)
                        .setMessage(activity.getString(R.string.alert_group_remove_text).replace("{0}", userVM.getName().get()))
                        .setPositiveButton(R.string.alert_group_remove_yes, (dialog2, which1) -> {
                            activity.execute(messenger().kickMember(groupVM.getId(), userVM.getId()),
                                    R.string.progress_common, new CommandCallback<Void>() {
                                        @Override
                                        public void onResult(Void res1) {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Toast.makeText(activity, R.string.toast_unable_kick, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show()
                        .setCanceledOnTouchOutside(true);
            });
        }
        if (groupVM.getIsCanEditAdmins().get() && !userVM.isBot()) {
            alertListBuilder.addItem(!isAdministrator ? activity.getResources().getString(R.string.group_make_admin) : activity.getResources().getString(R.string.group_revoke_admin), () -> {
                if (!isAdministrator) {
                    messenger().makeAdmin(groupVM.getId(), userVM.getId()).start(new CommandCallback<Void>() {
                        @Override
                        public void onResult(Void res) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                } else {
                    messenger().revokeAdmin(groupVM.getId(), userVM.getId()).start(new CommandCallback<Void>() {
                        @Override
                        public void onResult(Void res) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
        }
        alertListBuilder.build(activity)
                .show()
                .setCanceledOnTouchOutside(true);
    }
}
