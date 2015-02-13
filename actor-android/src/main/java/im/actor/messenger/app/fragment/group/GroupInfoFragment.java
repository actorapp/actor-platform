package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.mvvm.ui.Listener;

import java.util.Arrays;
import java.util.Comparator;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.ViewAvatarActivity;
import im.actor.messenger.app.base.BaseBarActivity;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.ViewHolder;
import im.actor.messenger.core.actors.base.UiAskCallback;
import im.actor.messenger.core.actors.groups.GroupsActor;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.model.GroupModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.storage.scheme.groups.GroupMember;
import im.actor.messenger.storage.scheme.groups.GroupState;
import im.actor.messenger.util.BoxUtil;
import im.actor.messenger.util.Screen;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.groups;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class GroupInfoFragment extends BaseCompatFragment {

    private static final String EXTRA_CHAT_ID = "chat_id";

    public static GroupInfoFragment create(int chatId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_CHAT_ID, chatId);
        GroupInfoFragment res = new GroupInfoFragment();
        res.setArguments(args);
        return res;
    }

    private int chatId;
    private GroupModel groupInfo;
    private ListView listView;
    private GroupUserAdapter groupUserAdapter;
    private CoverAvatarView avatarView;

    private Listener<GroupState> groupStateListener = new Listener<GroupState>() {
        @Override
        public void onUpdated(GroupState groupState) {
            GroupInfoFragment.this.onUpdated(groupState);
        }
    };

    private Listener<GroupMember[]> groupUsersListener = new Listener<GroupMember[]>() {
        @Override
        public void onUpdated(GroupMember[] groupState) {
            GroupInfoFragment.this.onUpdated(groupState);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_CHAT_ID);

        groupInfo = groups().get(chatId);

        View res = inflater.inflate(R.layout.fragment_group, container, false);

        listView = (ListView) res.findViewById(R.id.groupList);

        View header = inflater.inflate(R.layout.fragment_group_header, listView, false);

        // Avatar
        avatarView = (CoverAvatarView) header.findViewById(R.id.avatar);

        bind(groupInfo.getAvatarModel(), new Listener<Avatar>() {
            @Override
            public void onUpdated(Avatar avatar) {
                if (avatar != null) {
                    avatarView.request(avatar);
                } else {
                    avatarView.clear();
                }
            }
        });
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
            }
        });

        // Title
        getBinder().bindText((TextView) header.findViewById(R.id.title), groupInfo.getTitleModel());

        // Created by
        TextView createdBy = (TextView) header.findViewById(R.id.createdBy);
        if (groupInfo.getRaw().getAdminId() == myUid()) {
            createdBy.setText(R.string.group_created_by_you);
        } else {
            UserModel admin = users().get(groupInfo.getRaw().getAdminId());
            createdBy.setText(getString(R.string.group_created_by).replace("{0}", admin.getName()));
        }

        // Shared
        header.findViewById(R.id.filesCont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intents.openDocs(DialogType.TYPE_GROUP, chatId, getActivity()));
            }
        });

        int docsCount = ListEngines.getDocuments(DialogUids.getDialogUid(DialogType.TYPE_GROUP, chatId)).getCount();
        if (docsCount == 0) {
            header.findViewById(R.id.sharedTitle).setVisibility(View.GONE);
            header.findViewById(R.id.sharedDiv).setVisibility(View.GONE);
            header.findViewById(R.id.filesCont).setVisibility(View.GONE);
        } else {
            header.findViewById(R.id.sharedTitle).setVisibility(View.VISIBLE);
            header.findViewById(R.id.sharedDiv).setVisibility(View.VISIBLE);
            header.findViewById(R.id.filesCont).setVisibility(View.VISIBLE);

            ((TextView) header.findViewById(R.id.documentCount)).setText(
                    ListEngines.getDocuments(DialogUids.getDialogUid(DialogType.TYPE_GROUP, chatId)).getCount() + "");
        }

        ((TextView) header.findViewById(R.id.membersCount)).setText(
                getString(R.string.group_members_count)
                        .replace("{0}", groupInfo.getUsersCount() + "")
                        .replace("{1}", "300"));

        listView.addHeaderView(header, null, false);

        View add = inflater.inflate(R.layout.fragment_group_add, listView, false);
        add.findViewById(R.id.addUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Intents.pickUser(getActivity()), 0);
            }
        });
        listView.addFooterView(add, null, false);

        groupUserAdapter = new GroupUserAdapter(sort(convert(groupInfo.getUsers())), getActivity());

        listView.setAdapter(groupUserAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null && item instanceof UserModel) {
                    final UserModel userModel = (UserModel) item;
                    if (userModel.getId() == myUid()) {
                        return;
                    }
                    new AlertDialog.Builder(getActivity())
                            .setItems(new CharSequence[]{
                                    getString(R.string.group_context_message).replace("{0}", userModel.getName()),
                                    getString(R.string.group_context_call).replace("{0}", userModel.getName()),
                                    getString(R.string.group_context_view).replace("{0}", userModel.getName()),
                                    getString(R.string.group_context_remove).replace("{0}", userModel.getName()),
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        startActivity(Intents.openPrivateDialog(userModel.getId(), true, getActivity()));
                                    } else if (which == 1) {
                                        startActivity(Intents.call(userModel.getPhone()));
                                    } else if (which == 2) {
                                        startActivity(Intents.openProfile(userModel.getId(), getActivity()));
                                    } else if (which == 3) {
                                        new AlertDialog.Builder(getActivity())
                                                .setMessage(getString(R.string.alert_group_remove_text).replace("{0}", userModel.getName()))
                                                .setPositiveButton(R.string.alert_group_remove_yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog2, int which) {
                                                        ask(GroupsActor.groupUpdates().kickUser(chatId, userModel.getId()),
                                                                getString(R.string.group_removing),
                                                                new UiAskCallback<Boolean>() {

                                                                    @Override
                                                                    public void onPreStart() {

                                                                    }

                                                                    @Override
                                                                    public void onCompleted(Boolean res) {
                                                                        if (!res) {
                                                                            Toast.makeText(getActivity(), R.string.toast_unable_kick, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onError(Throwable t) {
                                                                        Toast.makeText(getActivity(), R.string.toast_unable_kick, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                })
                                                .setNegativeButton(R.string.dialog_cancel, null)
                                                .show()
                                                .setCanceledOnTouchOutside(true);
                                    }
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(true);
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >= 1) {
                    updateBar(Integer.MAX_VALUE);
                } else {
                    View top = listView.getChildAt(0);
                    if (top != null) {
                        updateBar(-top.getTop());
                    } else {
                        updateBar(Integer.MAX_VALUE);
                    }
                }
            }
        });

        bind(groupInfo.getStateModel(), groupStateListener);
        bind(groupInfo.getUsersModel(), groupUsersListener);

        return res;
    }

    public void updateBar(int offset) {

        avatarView.setOffset(offset);

        int baseColor = getResources().getColor(R.color.primary);

        if (offset > Screen.dp(248 - 56)) {
            ((BaseBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(baseColor));
        } else {
            float alpha = offset / (float) Screen.dp(248 - 56);

            int color = Color.argb((int) (255 * alpha),
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor));

            ((BaseBarActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(color));
        }
    }

    public void onUpdated(GroupState groupState) {
        if (groupState != GroupState.JOINED) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    public void onUpdated(GroupMember[] users) {
        groupUserAdapter.updateUid(sort(convert(users)));
    }

    private int[] sort(final int[] users) {
        Integer[] res = BoxUtil.box(users);
        Arrays.sort(res, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer integer2) {
                UserModel u1 = users().get(integer);
                UserModel u2 = users().get(integer2);
                return u1.getName().compareTo(u2.getName());
            }
        });
        return BoxUtil.unbox(res);
    }

    private int[] convert(GroupMember[] users) {
        int[] c = new int[users.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = users[i].getUid();
        }
        return c;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.group_info, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leaveGroup) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_delete_group_title).replace("{0}",
                            groupInfo.getTitle()))
                    .setPositiveButton(R.string.alert_delete_group_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            GroupsActor.groupUpdates().leaveChat(chatId);
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

            return true;
        } else if (item.getItemId() == R.id.addMember) {
            startActivityForResult(Intents.pickUser(getActivity()), 0);
        } else if (item.getItemId() == R.id.editTitle) {
            startActivity(Intents.editGroupTitle(chatId, getActivity()));
        } else if (item.getItemId() == R.id.changePhoto) {
            startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.hasExtra(Intents.EXTRA_UID)) {
            final UserModel userModel = users().get(data.getIntExtra(Intents.EXTRA_UID, 0));

            for (GroupMember uid : groupInfo.getUsers()) {
                if (uid.getUid() == userModel.getId()) {
                    Toast.makeText(getActivity(), R.string.toast_already_member, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_group_add_text).replace("{0}", userModel.getName()))
                    .setPositiveButton(R.string.alert_group_add_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            ask(GroupsActor.groupUpdates().addUser(chatId, userModel.getId()), getString(R.string.group_adding),
                                    new UiAskCallback<Boolean>() {

                                        @Override
                                        public void onPreStart() {

                                        }

                                        @Override
                                        public void onCompleted(Boolean res) {
                                            if (!res) {
                                                Toast.makeText(getActivity(), R.string.toast_unable_add, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable t) {
                                            Toast.makeText(getActivity(), R.string.toast_unable_add, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        groupUserAdapter = null;
        if (avatarView != null) {
            avatarView.clear();
            avatarView = null;
        }
    }

    private class GroupUserAdapter extends UserAdapter {

        public GroupUserAdapter(int[] uids, Context context) {
            super(uids, context);
        }

        @Override
        protected ViewHolder<UserModel> createHolder(UserModel obj) {
            return new GroupViewHolder();
        }
    }

    private class GroupViewHolder extends ViewHolder<UserModel> {

        private TextView userName;
        private View admin;
        private AvatarView avatarView;

        @Override
        public View init(UserModel data, ViewGroup viewGroup, Context context) {
            View res = getActivity().getLayoutInflater().inflate(R.layout.fragment_group_item, viewGroup, false);
            userName = (TextView) res.findViewById(R.id.name);
            avatarView = (AvatarView) res.findViewById(R.id.avatar);
            admin = res.findViewById(R.id.adminFlag);
            return res;
        }

        @Override
        public void bind(UserModel data, int position, Context context) {
            avatarView.setEmptyDrawable(AvatarDrawable.create(data, 16, getActivity()));
            Avatar avatar = data.getAvatar().getValue();
            if (avatar != null) {
                avatarView.bindAvatar(32, avatar);
            } else {
                avatarView.unbind();
            }
            userName.setText(data.getName());
            if (data.getId() == groupInfo.getRaw().getAdminId()) {
                admin.setVisibility(View.VISIBLE);
            } else {
                admin.setVisibility(View.GONE);
            }
        }
    }
}