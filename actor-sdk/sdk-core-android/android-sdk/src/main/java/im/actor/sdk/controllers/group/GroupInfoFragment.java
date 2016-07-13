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
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.messages.Void;
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
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.sdk.util.Fonts;

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
    private CoverAvatarView avatarView;
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

        res.setBackgroundColor(style.getBackyardBackgroundColor());
        listView.setBackgroundColor(style.getMainBackgroundColor());
        notMemberView.setBackgroundColor(style.getMainBackgroundColor());
        ((TextView) notMemberView.findViewById(R.id.not_member_text)).setTextColor(style.getTextPrimaryColor());

        //
        // Header
        //

        // Views
        View header = inflater.inflate(R.layout.fragment_group_header, listView, false);
        avatarView = (CoverAvatarView) header.findViewById(R.id.avatar);
        TextView aboutTV = (TextView) header.findViewById(R.id.about);
        View aboutCont = header.findViewById(R.id.aboutContainer);
        TextView title = (TextView) header.findViewById(R.id.title);
        TextView createdBy = (TextView) header.findViewById(R.id.createdBy);
        View descriptionContainer = header.findViewById(R.id.descriptionContainer);
        SwitchCompat isNotificationsEnabled = (SwitchCompat) header.findViewById(R.id.enableNotifications);
        TextView memberCount = (TextView) header.findViewById(R.id.membersCount);
        TextView settingsHeaderText = (TextView) header.findViewById(R.id.settings_header_text);
        TintImageView notificationSettingIcon = (TintImageView) header.findViewById(R.id.settings_notification_icon);
        TextView membersHeaderText = (TextView) header.findViewById(R.id.membersTitle);

        // Styling
        ((TextView) header.findViewById(R.id.about_hint)).setTextColor(style.getTextSecondaryColor());
        header.setBackgroundColor(style.getMainBackgroundColor());
        title.setTextColor(style.getProfileTitleColor());
        createdBy.setTextColor(style.getProfileSubtitleColor());
        aboutTV.setTextColor(style.getTextPrimaryColor());
        // themeHeader.setTextColor(style.getProfileSubtitleColor());
        memberCount.setTextColor(style.getTextHintColor());
        settingsHeaderText.setTextColor(style.getSettingsCategoryTextColor());
        notificationSettingIcon.setTint(style.getSettingsIconColor());
        membersHeaderText.setTextColor(style.getSettingsCategoryTextColor());
        ((TextView) header.findViewById(R.id.settings_notifications_title)).setTextColor(style.getTextPrimaryColor());
        header.findViewById(R.id.after_about_divider).setBackgroundColor(style.getBackyardBackgroundColor());
        header.findViewById(R.id.after_settings_divider).setBackgroundColor(style.getBackyardBackgroundColor());


        // Avatar
        bind(avatarView, groupVM.getAvatar());
        avatarView.setOnClickListener(view -> {
            startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
        });

        // Title
        bind(title, groupVM.getName());

        // Owned by
        bind(groupVM.getOwnerId(), ownerId -> {
            if (ownerId != 0) {
                if (ownerId == myUid()) {
                    createdBy.setText(R.string.group_created_by_you);
                } else {
                    String ownerName = users().get(ownerId).getName().get();
                    createdBy.setText(getString(R.string.group_created_by).replace("{0}", ownerName));
                }
            } else {
                createdBy.setText("");
            }
        });

        // About

        bind(groupVM.getOwnerId(), groupVM.getAbout(), (ownerId, valueModel, theme, valueModel2) -> {
            boolean isVisible;
            if (theme != null) {
                isVisible = true;
                aboutTV.setText(theme);
            } else {
                aboutTV.setText(R.string.about_group_empty);
                isVisible = ownerId == myUid();
            }
            descriptionContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);

            if (ownerId == myUid()) {
                aboutCont.setOnClickListener(view -> {
                    startActivity(Intents.editGroupAbout(groupVM.getId(), getActivity()));
                });
            } else {
                aboutCont.setOnClickListener(null);
            }
        });

        // Notifications

        isNotificationsEnabled.setChecked(messenger().isNotificationsEnabled(Peer.group(chatId)));
        isNotificationsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            messenger().changeNotificationsEnabled(Peer.group(chatId), isChecked);
        });
        header.findViewById(R.id.notificationsCont).setOnClickListener(v -> {
            isNotificationsEnabled.setChecked(!isNotificationsEnabled.isChecked());
        });

        // Members

        bind(groupVM.getMembersCount(), val -> {
            if (val != null) {
                memberCount.setText(val + "");
            } else {
                memberCount.setText("");
            }
        });

        listView.addHeaderView(header, null, false);


        //
        // Footer
        //

        // View
        View footer = inflater.inflate(R.layout.fragment_group_add, listView, false);
        TextView name = (TextView) footer.findViewById(R.id.name);
        TintImageView addIcon = (TintImageView) footer.findViewById(R.id.add_icon);

        // Style
        footer.findViewById(R.id.bottom_divider).setBackgroundColor(style.getBackyardBackgroundColor());
        name.setTextColor(style.getActionAddContactColor());
        name.setTypeface(Fonts.medium());
        addIcon.setTint(style.getGroupActionAddIconColor());
        addIcon.setTint(style.getActionAddContactColor());

        footer.findViewById(R.id.addUser).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddMemberActivity.class)
                    .putExtra("GROUP_ID", chatId));
        });

        listView.addFooterView(footer, null, false);


        //
        // Members
        //

        groupUserAdapter = new MembersAdapter(getActivity());
        bind(groupVM.getMembers(), members -> {
            groupUserAdapter.setMembers(members);
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

        return res;
    }

    public void onMemberClicked(UserVM userVM) {

        boolean isOwned = groupVM.getOwnerId().get() == myUid();

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

    public void updateBar(int offset) {

        avatarView.setOffset(offset);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        if (groupVM.isMember().get()) {
            menuInflater.inflate(R.menu.group_info, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leaveGroup) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.alert_leave_group_message).replace("%1$s",
                            groupVM.getName().get()))
                    .setPositiveButton(R.string.alert_leave_group_yes, (dialog2, which) -> {
                        execute(messenger().leaveGroup(chatId));
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
        }
        return super.onOptionsItemSelected(item);
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