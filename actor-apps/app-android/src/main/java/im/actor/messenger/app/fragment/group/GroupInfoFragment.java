package im.actor.messenger.app.fragment.group;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.HashSet;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.ViewAvatarActivity;
import im.actor.messenger.app.base.BaseActivity;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.fragment.group.view.MembersAdapter;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.GroupMember;
import im.actor.model.entity.Peer;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.viewmodel.GroupVM;
import im.actor.model.viewmodel.UserPhone;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.groups;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;

/**
 * Created by ex3ndr on 07.10.14.
 */
public class GroupInfoFragment extends BaseFragment {

    private static final String EXTRA_CHAT_ID = "chat_id";

    public static GroupInfoFragment create(int chatId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_CHAT_ID, chatId);
        GroupInfoFragment res = new GroupInfoFragment();
        res.setArguments(args);
        return res;
    }

    private int chatId;
    private GroupVM groupInfo;
    private ListView listView;
    private MembersAdapter groupUserAdapter;
    private CoverAvatarView avatarView;
    private View notMemberView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_CHAT_ID);

        groupInfo = groups().get(chatId);

        View res = inflater.inflate(R.layout.fragment_group, container, false);

        notMemberView = res.findViewById(R.id.notMember);

        bind(groupInfo.isMember(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, ValueModel<Boolean> valueModel) {
                notMemberView.setVisibility(val ? View.GONE : View.VISIBLE);
                getActivity().invalidateOptionsMenu();
            }
        });

        listView = (ListView) res.findViewById(R.id.groupList);

        View header = inflater.inflate(R.layout.fragment_group_header, listView, false);

        // Avatar
        avatarView = (CoverAvatarView) header.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) header.findViewById(R.id.avatar_bgrnd));
        bind(avatarView, groupInfo.getAvatar());
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
            }
        });

        // Title
        bind((TextView) header.findViewById(R.id.title), groupInfo.getName());

        // Created by
        final TextView createdBy = (TextView) header.findViewById(R.id.createdBy);
        if (groupInfo.getCreatorId() == myUid()) {
            createdBy.setText(R.string.group_created_by_you);
        } else {
            UserVM admin = users().get(groupInfo.getCreatorId());
            bind(admin.getName(), new ValueChangedListener<String>() {
                @Override
                public void onChanged(String val, ValueModel<String> valueModel) {
                    createdBy.setText(getString(R.string.group_created_by).replace("{0}", val));
                }
            });
        }

        // Settings

        final SwitchCompat isNotificationsEnabled = (SwitchCompat) header.findViewById(R.id.enableNotifications);
        isNotificationsEnabled.setChecked(messenger().isNotificationsEnabled(Peer.group(chatId)));
        isNotificationsEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                messenger().changeNotificationsEnabled(Peer.group(chatId), isChecked);
            }
        });
        header.findViewById(R.id.notificationsCont).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNotificationsEnabled.setChecked(!isNotificationsEnabled.isChecked());
            }
        });


        // Media
        int docsCount = 0;//ListEngines.getDocuments(DialogUids.getDialogUid(DialogType.TYPE_GROUP, chatId)).getCount();
        if (docsCount == 0) {
            header.findViewById(R.id.docsContainer).setVisibility(View.GONE);
        } else {
            header.findViewById(R.id.sharedContainer).setVisibility(View.VISIBLE);
            header.findViewById(R.id.docsContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intents.openDocs(Peer.group(groupInfo.getId()), getActivity()));
                }
            });
            ((TextView) header.findViewById(R.id.docCount)).setText(
                    "" + docsCount
            );
        }

        Peer peer = Peer.group(groupInfo.getId());
        int mediaCount = 0;//messenger().getMediaCount(peer);
        if (mediaCount == 0) {
            header.findViewById(R.id.mediaContainer).setVisibility(View.GONE);
        } else {
            header.findViewById(R.id.sharedContainer).setVisibility(View.VISIBLE);
            header.findViewById(R.id.mediaContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intents.openMedias(Peer.group(groupInfo.getId()), getActivity()));
                }
            });
            header.findViewById(R.id.mediaCount).setVisibility(View.VISIBLE);
            ((TextView) header.findViewById(R.id.mediaCount)).setText(
                    "" + mediaCount
            );
        }
        // header.findViewById(R.id.sharedContainer).setVisibility(View.GONE);


        //Members
        ((TextView) header.findViewById(R.id.membersCount)).setText(
                getString(R.string.group_members_count)
                        .replace("{0}", groupInfo.getMembersCount() + "")
                        .replace("{1}", "300"));

        listView.addHeaderView(header, null, false);

        View add = inflater.inflate(R.layout.fragment_group_add, listView, false);
        ((TextView) add.findViewById(R.id.name)).setTypeface(Fonts.medium());

        add.findViewById(R.id.addUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddMemberActivity.class)
                        .putExtra("GROUP_ID", chatId));
            }
        });
        listView.addFooterView(add, null, false);

        groupUserAdapter = new MembersAdapter(groupInfo.getMembers().get(), getActivity());
        bind(groupInfo.getMembers(), new ValueChangedListener<HashSet<GroupMember>>() {
            @Override
            public void onChanged(HashSet<GroupMember> val, ValueModel<HashSet<GroupMember>> valueModel) {
                groupUserAdapter.updateUid(val);
            }
        });
        listView.setAdapter(groupUserAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item != null && item instanceof GroupMember) {
                    final GroupMember groupMember = (GroupMember) item;
                    if (groupMember.getUid() == myUid()) {
                        return;
                    }
                    final UserVM userVM = users().get(groupMember.getUid());
                    if (userVM == null) {
                        return;
                    }
                    new AlertDialog.Builder(getActivity())
                            .setItems(new CharSequence[]{
                                    getString(R.string.group_context_message).replace("{0}", userVM.getName().get()),
                                    getString(R.string.group_context_call).replace("{0}", userVM.getName().get()),
                                    getString(R.string.group_context_view).replace("{0}", userVM.getName().get()),
                                    getString(R.string.group_context_remove).replace("{0}", userVM.getName().get()),
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        startActivity(Intents.openPrivateDialog(userVM.getId(), true, getActivity()));
                                    } else if (which == 1) {
                                        final ArrayList<UserPhone> phones = userVM.getPhones().get();
                                        if (phones.size() == 0) {
                                            Toast.makeText(getActivity(), "No phones available", Toast.LENGTH_SHORT).show();
                                        } else if (phones.size() == 1) {
                                            startActivity(Intents.call(phones.get(0).getPhone()));
                                        } else {
                                            CharSequence[] sequences = new CharSequence[phones.size()];
                                            for (int i = 0; i < sequences.length; i++) {
                                                try {
                                                    Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + phones.get(i).getPhone(), "us");
                                                    sequences[i] = phones.get(which).getTitle() + ": " + PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                                                } catch (NumberParseException e) {
                                                    e.printStackTrace();
                                                    sequences[i] = phones.get(which).getTitle() + ": +" + phones.get(i).getPhone();
                                                }
                                            }
                                            new AlertDialog.Builder(getActivity())
                                                    .setItems(sequences, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            startActivity(Intents.call(phones.get(which).getPhone()));
                                                        }
                                                    })
                                                    .show()
                                                    .setCanceledOnTouchOutside(true);
                                        }
                                    } else if (which == 2) {
                                        startActivity(Intents.openProfile(userVM.getId(), getActivity()));
                                    } else if (which == 3) {
                                        new AlertDialog.Builder(getActivity())
                                                .setMessage(getString(R.string.alert_group_remove_text).replace("{0}", userVM.getName().get()))
                                                .setPositiveButton(R.string.alert_group_remove_yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog2, int which) {
                                                        execute(messenger().kickMember(chatId, userVM.getId()),
                                                                R.string.progress_common, new CommandCallback<Boolean>() {
                                                                    @Override
                                                                    public void onResult(Boolean res) {

                                                                    }

                                                                    @Override
                                                                    public void onError(Exception e) {
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

        return res;
    }

    public void updateBar(int offset) {

        avatarView.setOffset(offset);

        int baseColor = getResources().getColor(R.color.primary);

        if (offset > Screen.dp(248 - 56)) {
            ((BaseActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(baseColor));
        } else {
            float alpha = offset / (float) Screen.dp(248 - 56);

            int color = Color.argb((int) (255 * alpha),
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor));

            ((BaseActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(color));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (groupInfo.isMember().get()) {
            menuInflater.inflate(R.menu.group_info, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leaveGroup) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_leave_group_message).replace("%1$s",
                            groupInfo.getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            execute(messenger().leaveGroup(chatId));
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);

            return true;
        } else if (item.getItemId() == R.id.addMember) {
            startActivity(new Intent(getActivity(), AddMemberActivity.class)
                    .putExtra("GROUP_ID", chatId));
        } else if (item.getItemId() == R.id.editTitle) {
            startActivity(Intents.editGroupTitle(chatId, getActivity()));
        } else if (item.getItemId() == R.id.changePhoto) {
            startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
        } else if (item.getItemId() == R.id.integrationToken) {
            startActivity(Intents.integrationToken(chatId, getActivity()));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 0 && data != null && data.hasExtra(Intents.EXTRA_UID)) {
            final UserVM userModel = users().get(data.getIntExtra(Intents.EXTRA_UID, 0));

            for (GroupMember uid : groupInfo.getMembers().get()) {
                if (uid.getUid() == userModel.getId()) {
                    Toast.makeText(getActivity(), R.string.toast_already_member, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_group_add_text).replace("{0}", userModel.getName().get()))
                    .setPositiveButton(R.string.alert_group_add_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog2, int which) {
                            execute(messenger().inviteMember(chatId, userModel.getId()),
                                    R.string.progress_common, new CommandCallback<Boolean>() {
                                        @Override
                                        public void onResult(Boolean res) {

                                        }

                                        @Override
                                        public void onError(Exception e) {
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
            avatarView.unbind();
            avatarView = null;
        }
    }
}