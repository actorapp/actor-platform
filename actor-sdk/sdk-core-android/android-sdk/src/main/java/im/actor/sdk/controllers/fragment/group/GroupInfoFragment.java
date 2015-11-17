package im.actor.sdk.controllers.fragment.group;

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

import im.actor.core.entity.GroupMember;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.fragment.ActorBinder;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.controllers.fragment.group.view.MembersAdapter;
import im.actor.sdk.controllers.fragment.media.DocumentsActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.sdk.util.Fonts;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.util.ActorSDKMessenger.groups;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class GroupInfoFragment extends BaseFragment {

    private static final String EXTRA_CHAT_ID = "chat_id";
    private String[] theme;
    private String[] about;
    private int chatId;
    private GroupVM groupInfo;
    private ListView listView;
    private MembersAdapter groupUserAdapter;
    private CoverAvatarView avatarView;
    private View notMemberView;

    public static GroupInfoFragment create(int chatId) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_CHAT_ID, chatId);
        GroupInfoFragment res = new GroupInfoFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        chatId = getArguments().getInt(EXTRA_CHAT_ID);

        groupInfo = groups().get(chatId);

        View res = inflater.inflate(R.layout.fragment_group, container, false);

        notMemberView = res.findViewById(R.id.notMember);
        ((TextView) notMemberView.findViewById(R.id.not_member_text)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        bind(groupInfo.isMember(), new ValueChangedListener<Boolean>() {
            @Override
            public void onChanged(Boolean val, Value<Boolean> Value) {
                notMemberView.setVisibility(val ? View.GONE : View.VISIBLE);
                getActivity().invalidateOptionsMenu();
            }
        });

        listView = (ListView) res.findViewById(R.id.groupList);
        listView.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        final View header = inflater.inflate(R.layout.fragment_group_header, listView, false);
        header.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());

        // Avatar
        avatarView = (CoverAvatarView) header.findViewById(R.id.avatar);
        ImageView avatarBkgrnd = (ImageView) header.findViewById(R.id.avatar_bgrnd);
        avatarBkgrnd.setBackgroundColor(ActorSDK.sharedActor().style.getAvatarBackgroundColor());
        avatarView.setBkgrnd(avatarBkgrnd);
        bind(avatarView, groupInfo.getAvatar());
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ViewAvatarActivity.viewGroupAvatar(chatId, getActivity()));
            }
        });

        ActorStyle style = ActorSDK.sharedActor().style;
        // Title
        TextView title = (TextView) header.findViewById(R.id.title);
        title.setTextColor(style.getProfileTitleColor());
        bind(title, groupInfo.getName());

        // Created by
        boolean isAdmin = false;
        final TextView createdBy = (TextView) header.findViewById(R.id.createdBy);
        createdBy.setTextColor(style.getProfileSubtitleColor());
        if (groupInfo.getCreatorId() == myUid()) {
            createdBy.setText(R.string.group_created_by_you);
            isAdmin = true;
        } else {
            UserVM admin = users().get(groupInfo.getCreatorId());
            bind(admin.getName(), new ValueChangedListener<String>() {
                @Override
                public void onChanged(String val, Value<String> Value) {
                    createdBy.setText(getString(R.string.group_created_by).replace("{0}", val));
                }
            });
        }

        //Description
        theme = new String[1];
        about = new String[1];
        TextView themeTV = (TextView) header.findViewById(R.id.theme);
        themeTV.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) header.findViewById(R.id.group_theme_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        TextView aboutTV = (TextView) header.findViewById(R.id.about);
        aboutTV.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) header.findViewById(R.id.about_hint)).setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        final View descriptionContainer = header.findViewById(R.id.descriptionContainer);
        final TextView themeHeader = (TextView) header.findViewById(R.id.theme_header);
        themeHeader.setTextColor(style.getProfileSubtitleColor());

        final boolean finalIsAdmin = isAdmin;
        bind(themeTV, header.findViewById(R.id.themeContainer), groupInfo.getTheme(), new ActorBinder.OnChangedListener<String>() {
            @Override
            public void onChanged(String s) {
                theme[0] = s;
                updateDescriptionVisibility(descriptionContainer, finalIsAdmin, header);
            }
        }, !isAdmin, getString(R.string.theme_group_empty));

        //bind(themeHeader, themeHeader, groupInfo.getTheme());

        bind(aboutTV, header.findViewById(R.id.aboutContainer), groupInfo.getAbout(), new ActorBinder.OnChangedListener<String>() {
            @Override
            public void onChanged(String s) {
                about[0] = s;
                updateDescriptionVisibility(descriptionContainer, finalIsAdmin, header);
            }
        }, !isAdmin, getString(R.string.about_group_empty));

        if (isAdmin) {
            header.findViewById(R.id.themeContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intents.editGroupTheme(groupInfo.getId(), getActivity()));
                }
            });

            header.findViewById(R.id.aboutContainer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intents.editGroupAbout(groupInfo.getId(), getActivity()));
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


        header.findViewById(R.id.docsContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DocumentsActivity.build(Peer.group(chatId), getActivity()));
            }
        });


        header.findViewById(R.id.mediaContainer).setVisibility(View.GONE);

        //Members
        TextView memberCount = (TextView) header.findViewById(R.id.membersCount);
        memberCount.setText(
                getString(R.string.group_members_count)
                        .replace("{0}", groupInfo.getMembersCount() + "")
                        .replace("{1}", "300"));
        memberCount.setTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        listView.addHeaderView(header, null, false);

        View add = inflater.inflate(R.layout.fragment_group_add, listView, false);
        add.findViewById(R.id.bottom_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        TextView name = (TextView) add.findViewById(R.id.name);
        name.setTextColor(ActorSDK.sharedActor().style.getActionAddContactColor());
        name.setTypeface(Fonts.medium());
        TintImageView addIcon = (TintImageView) add.findViewById(R.id.add_icon);
        addIcon.setTint(ActorSDK.sharedActor().style.getGroupActionAddIconColor());
        addIcon.setTint(ActorSDK.sharedActor().style.getActionAddContactColor());

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
            public void onChanged(HashSet<GroupMember> val, Value<HashSet<GroupMember>> Value) {
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

        res.findViewById(R.id.after_about_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        res.findViewById(R.id.after_settings_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        res.findViewById(R.id.bottom_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());


        TextView settingsHeaderText = (TextView) res.findViewById(R.id.settings_header_text);
        settingsHeaderText.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());

        TintImageView notificationSettingIcon = (TintImageView) res.findViewById(R.id.settings_notification_icon);
        notificationSettingIcon.setTint(style.getSettingsIconColor());
        ((TextView) res.findViewById(R.id.settings_notifications_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());

        TintImageView shareMediaIcon = (TintImageView) res.findViewById(R.id.share_media_icon);
        shareMediaIcon.setTint(style.getSettingsIconColor());
        ((TextView) res.findViewById(R.id.settings_media_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.mediaCount)).setTextColor(ActorSDK.sharedActor().style.getTextHintColor());

        TintImageView shareDocsIcon = (TintImageView) res.findViewById(R.id.share_docs_icon);
        shareDocsIcon.setTint(style.getSettingsIconColor());
        ((TextView) res.findViewById(R.id.share_docs_title)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.docCount)).setTextColor(ActorSDK.sharedActor().style.getTextHintColor());

        TextView sharedHeaderText = (TextView) res.findViewById(R.id.shared_header_text);
        sharedHeaderText.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());

        TextView membersHeaderText = (TextView) res.findViewById(R.id.membersTitle);
        membersHeaderText.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());

        return res;
    }

    public void updateDescriptionVisibility(View descriptionContainer, boolean finalIsAdmin, View header) {
        View themeDivider = header.findViewById(R.id.themeDivider);
        themeDivider.setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        boolean themeVis = theme[0] != null && !theme[0].isEmpty();
        boolean aboutVis = about[0] != null && !about[0].isEmpty();

        descriptionContainer.setVisibility((aboutVis || themeVis || finalIsAdmin) ? View.VISIBLE : View.GONE);
        themeDivider.setVisibility((themeVis && aboutVis) ? View.VISIBLE : View.GONE);

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