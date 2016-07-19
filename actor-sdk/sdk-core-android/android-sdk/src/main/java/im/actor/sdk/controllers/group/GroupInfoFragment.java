package im.actor.sdk.controllers.group;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.core.entity.GroupType;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.ValueDoubleListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKLauncher;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.group.view.MembersAdapter;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.adapters.RecyclerListView;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

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
    private GroupVM groupVM;

    private RecyclerListView listView;
    private AvatarView avatarView;
    private MembersAdapter groupUserAdapter;
    private View notMemberView;

    public GroupInfoFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
        setShowHome(true);
    }

    @Override
    public void onConfigureActionBar(ActionBar actionBar) {
        super.onConfigureActionBar(actionBar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_CHAT_ID);

        groupVM = groups().get(chatId);

        View res = inflater.inflate(R.layout.fragment_group, container, false);
        listView = (RecyclerListView) res.findViewById(R.id.groupList);
        notMemberView = res.findViewById(R.id.notMember);

        res.setBackgroundColor(style.getMainBackgroundColor());
        // listView.setBackgroundColor(style.getMainBackgroundColor());
        notMemberView.setBackgroundColor(style.getMainBackgroundColor());
        ((TextView) notMemberView.findViewById(R.id.not_member_text)).setTextColor(style.getTextPrimaryColor());

        //
        // Header
        //

        // Views
        View header = inflater.inflate(R.layout.fragment_group_header, listView, false);
        TextView title = (TextView) header.findViewById(R.id.title);
        TextView subtitle = (TextView) header.findViewById(R.id.subtitle);
        avatarView = (AvatarView) header.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(48), 22);

        TextView aboutTV = (TextView) header.findViewById(R.id.about);
        View shortNameCont = header.findViewById(R.id.shortNameContainer);
        TextView shortNameView = (TextView) header.findViewById(R.id.shortName);
        TextView shortLinkView = (TextView) header.findViewById(R.id.shortNameLink);

        TextView addMember = (TextView) header.findViewById(R.id.addMemberAction);
        TextView members = (TextView) header.findViewById(R.id.viewMembersAction);
        TextView leaveAction = (TextView) header.findViewById(R.id.leaveAction);
        TextView administrationAction = (TextView) header.findViewById(R.id.administrationAction);

        View descriptionContainer = header.findViewById(R.id.descriptionContainer);
        SwitchCompat isNotificationsEnabled = (SwitchCompat) header.findViewById(R.id.enableNotifications);

        // Styling
        // ((TextView) header.findViewById(R.id.about_hint)).setTextColor(style.getTextSecondaryColor());
        header.setBackgroundColor(style.getMainBackgroundColor());
        header.findViewById(R.id.avatarContainer).setBackgroundColor(style.getToolBarColor());
        title.setTextColor(style.getProfileTitleColor());
        subtitle.setTextColor(style.getProfileSubtitleColor());
        aboutTV.setTextColor(style.getTextPrimaryColor());
        shortNameView.setTextColor(style.getTextPrimaryColor());
        shortLinkView.setTextColor(style.getTextSecondaryColor());
        // settingsHeaderText.setTextColor(style.getSettingsCategoryTextColor());

        ((TintImageView) header.findViewById(R.id.settings_notification_icon))
                .setTint(style.getSettingsIconColor());
        ((TintImageView) header.findViewById(R.id.settings_about_icon))
                .setTint(style.getSettingsIconColor());
        ((TextView) header.findViewById(R.id.settings_notifications_title))
                .setTextColor(style.getTextPrimaryColor());
        ((TextView) header.findViewById(R.id.addMemberAction))
                .setTextColor(style.getTextPrimaryColor());
        members.setTextColor(style.getTextPrimaryColor());
        administrationAction.setTextColor(style.getTextPrimaryColor());
        leaveAction.setTextColor(style.getTextDangerColor());


        if (groupVM.getGroupType() == GroupType.CHANNEL) {
            leaveAction.setText(R.string.group_leave_channel);
        } else {
            leaveAction.setText(R.string.group_leave);
        }

        header.findViewById(R.id.after_settings_divider).setBackgroundColor(style.getBackyardBackgroundColor());

        //
        // Header
        //
        avatarView.bind(groupVM.getAvatar().get(), groupVM.getName().get(), groupVM.getId());
        avatarView.setOnClickListener(view -> {
            if (groupVM.getAvatar().get() != null) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
            }
        });
        bind(groupVM.getName(), name -> {
            title.setText(name);
        });
        bind(groupVM.getMembersCount(), val -> {
            subtitle.setText(messenger().getFormatter().formatGroupMembers(val));
        });

        // About
        bind(groupVM.getAbout(), (about) -> {
            aboutTV.setText(about);
            aboutTV.setVisibility(about != null ? View.VISIBLE : View.GONE);
        });
        bind(groupVM.getShortName(), shortName -> {
            if (shortName != null) {
                shortNameView.setText("@" + shortName);
                shortLinkView.setText("actor.im/join/" + shortName);
            }
            shortNameCont.setVisibility(shortName != null ? View.VISIBLE : View.GONE);
        });
        bind(groupVM.getAbout(), groupVM.getShortName(), (about, shortName) -> {
            descriptionContainer.setVisibility(about != null || shortName != null
                    ? View.VISIBLE
                    : View.GONE);
        });

        // Notifications
        isNotificationsEnabled.setChecked(messenger().isNotificationsEnabled(Peer.group(chatId)));
        isNotificationsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            messenger().changeNotificationsEnabled(Peer.group(chatId), isChecked);
        });
        header.findViewById(R.id.notificationsCont).setOnClickListener(v -> {
            isNotificationsEnabled.setChecked(!isNotificationsEnabled.isChecked());
        });

        // Add Member
        bind(groupVM.getIsCanInviteMembers(), (canInvite) -> {
            if (canInvite) {
                addMember.setVisibility(View.VISIBLE);
            } else {
                addMember.setVisibility(View.GONE);
            }
        });
        addMember.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), AddMemberActivity.class)
                    .putExtra(Intents.EXTRA_GROUP_ID, chatId));
        });

        // Administration
        bind(groupVM.getIsCanEditAdministration(), groupVM.getIsCanEditShortName(), (canEditAdministration, valueModel, canEditShortName, valueModel2) -> {
            if (canEditAdministration || canEditShortName) {
                administrationAction.setVisibility(View.VISIBLE);
            } else {
                administrationAction.setVisibility(View.GONE);
            }
        });
        administrationAction.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), GroupAdminActivity.class)
                    .putExtra(Intents.EXTRA_GROUP_ID, chatId));
        });

        // Async Members
        // Showing member only when members available and async members is enabled
        bind(groupVM.getIsCanViewMembers(), groupVM.getIsAsyncMembers(), (canViewMembers, vm1, isAsync, vm2) -> {
            if (canViewMembers) {
                if (isAsync) {
                    members.setVisibility(View.VISIBLE);
                    header.findViewById(R.id.after_settings_divider).setVisibility(View.GONE);
                } else {
                    members.setVisibility(View.GONE);
                    header.findViewById(R.id.after_settings_divider).setVisibility(View.VISIBLE);
                }
            } else {
                header.findViewById(R.id.after_settings_divider).setVisibility(View.GONE);
            }
        });
        members.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), MembersActivity.class)
                    .putExtra(Intents.EXTRA_GROUP_ID, groupVM.getId()));
        });

        // Leave
        leaveAction.setOnClickListener(view1 -> {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_leave_group_message).replace("%1$s",
                            groupVM.getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, (dialog2, which) -> {
                        execute(messenger().leaveGroup(chatId));
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                    .setCanceledOnTouchOutside(true);
        });

        listView.addHeaderView(header, null, false);

        //
        // Members
        //

        groupUserAdapter = new MembersAdapter(getActivity());
        bind(groupVM.getIsAsyncMembers(), groupVM.getMembers(), (isAsyncMembers, valueModel, memberList, valueModel2) -> {
            if (isAsyncMembers) {
                groupUserAdapter.setMembers(new ArrayList<>());
            } else {
                groupUserAdapter.setMembers(memberList);
            }
        });
        listView.setAdapter(groupUserAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item != null && item instanceof GroupMember) {
                GroupMember groupMember = (GroupMember) item;
                if (groupMember.getUid() != myUid()) {
                    UserVM userVM = users().get(groupMember.getUid());
                    if (userVM != null) {
                        startActivity(Intents.openPrivateDialog(userVM.getId(), true, getActivity()));
                    }
                }
            }
        });
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Object item = adapterView.getItemAtPosition(i);
            if (item != null && item instanceof GroupMember) {
                GroupMember groupMember = (GroupMember) item;
                if (groupMember.getUid() != myUid()) {
                    UserVM userVM = users().get(groupMember.getUid());
                    if (userVM != null) {
                        onMemberClicked(userVM);
                        return true;
                    }
                }
            }
            return false;
        });

        //
        // Scroll handling
        //

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

        //
        // Placeholder
        //

        bind(groupVM.isMember(), (isMember) -> {
            notMemberView.setVisibility(isMember ? View.GONE : View.VISIBLE);
            getActivity().invalidateOptionsMenu();
        });

        // Menu
        bind(groupVM.getIsCanEditInfo(), canEditInfo -> {
            getActivity().invalidateOptionsMenu();
        });

        return res;
    }

    public void onMemberClicked(UserVM userVM) {
        new AlertDialog.Builder(getActivity())
                .setItems(new CharSequence[]{
                        getString(R.string.group_context_message).replace("{0}", userVM.getName().get()),
                        getString(R.string.group_context_call).replace("{0}", userVM.getName().get()),
                        getString(R.string.group_context_view).replace("{0}", userVM.getName().get()),
                        getString(R.string.group_context_remove).replace("{0}", userVM.getName().get()),
                }, (dialog, which) -> {
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
                                    .setItems(sequences, (dialog1, which1) -> {
                                        startActivity(Intents.call(phones.get(which1).getPhone()));
                                    })
                                    .show()
                                    .setCanceledOnTouchOutside(true);
                        }
                    } else if (which == 2) {
                        ActorSDKLauncher.startProfileActivity(getActivity(), userVM.getId());
                    } else if (which == 3) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.alert_group_remove_text).replace("{0}", userVM.getName().get()))
                                .setPositiveButton(R.string.alert_group_remove_yes, (dialog2, which1) -> {
                                    execute(messenger().kickMember(chatId, userVM.getId()),
                                            R.string.progress_common, new CommandCallback<Void>() {
                                                @Override
                                                public void onResult(Void res1) {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Toast.makeText(getActivity(), R.string.toast_unable_kick, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show()
                                .setCanceledOnTouchOutside(true);
                    }
                })
                .show()
                .setCanceledOnTouchOutside(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (groupVM.getIsCanEditInfo().get()) {
            MenuItem menuItem = menu.add(Menu.NONE, R.id.edit, Menu.NONE, R.string.actor_menu_edit);
            menuItem.setIcon(R.drawable.ic_edit_white_24dp);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edit) {
            startActivity(new Intent(getContext(), GroupEditActivity.class)
                    .putExtra(Intents.EXTRA_GROUP_ID, groupVM.getId()));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void updateBar(int offset) {

        int baseColor = getResources().getColor(R.color.primary);
        ActorStyle style = ActorSDK.sharedActor().style;
        if (style.getToolBarColor() != 0) {
            baseColor = style.getToolBarColor();
        }

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
    public void onDestroyView() {
        super.onDestroyView();
        if (groupUserAdapter != null) {
            groupUserAdapter.dispose();
            groupUserAdapter = null;
        }
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}