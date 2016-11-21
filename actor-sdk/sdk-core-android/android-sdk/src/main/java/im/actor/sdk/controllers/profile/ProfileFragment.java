package im.actor.sdk.controllers.profile;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.UserEmail;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.compose.ComposeActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.util.ViewUtils;
import im.actor.sdk.view.avatar.AvatarView;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ProfileFragment extends BaseFragment {

    public static int SOUND_PICKER_REQUEST_CODE = 122;

    public static final String EXTRA_UID = "uid";
    private View recordFieldWithIcon;

    public static ProfileFragment create(int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        ProfileFragment res = new ProfileFragment();
        res.setArguments(bundle);
        return res;
    }

    private AvatarView avatarView;
    private int uid;

    public ProfileFragment() {
        setRootFragment(true);
        setHomeAsUp(true);
        setTitle(null);
    }

    @Override
    public void onConfigureActionBar(ActionBar actionBar) {
        super.onConfigureActionBar(actionBar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        uid = getArguments().getInt(EXTRA_UID);

        final UserVM user = users().get(uid);
        ArrayList<UserPhone> phones = user.getPhones().get();
        ArrayList<UserEmail> emails = user.getEmails().get();
        String about = user.getAbout().get();
        final String userName = user.getNick().get();

        final View res = inflater.inflate(R.layout.fragment_profile, container, false);


        //
        // Style Background
        //

        res.findViewById(R.id.container).setBackgroundColor(style.getMainBackgroundColor());
        res.findViewById(R.id.avatarContainer).setBackgroundColor(style.getToolBarColor());


        //
        // User Avatar
        //

        avatarView = (AvatarView) res.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(48), 22);
        avatarView.bind(user.getAvatar().get(), user.getName().get(), user.getId());
        avatarView.setOnClickListener(v -> {
            startActivity(ViewAvatarActivity.viewAvatar(user.getId(), getActivity()));
        });


        //
        // User Name
        //

        TextView nameText = (TextView) res.findViewById(R.id.name);
        nameText.setTextColor(style.getProfileTitleColor());
        bind(nameText, user.getName());


        //
        // User Last Seen
        //

        TextView lastSeen = (TextView) res.findViewById(R.id.lastSeen);
        lastSeen.setTextColor(style.getProfileSubtitleColor());
        bind(lastSeen, user);

        //
        // Fab
        //

        FloatingActionButton fab = (FloatingActionButton) res.findViewById(R.id.fab);

        fab.setBackgroundTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_pressed},
                StateSet.WILD_CARD,

        }, new int[]{
                ActorSDK.sharedActor().style.getFabPressedColor(),
                ActorSDK.sharedActor().style.getFabColor(),
        }));
        fab.setRippleColor(ActorSDK.sharedActor().style.getFabPressedColor());
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), ComposeActivity.class)));

        //
        // Remove Contact
        //

        final View removeContact = res.findViewById(R.id.addContact);
        final TextView addContactTitle = (TextView) removeContact.findViewById(R.id.addContactTitle);
        addContactTitle.setText(getString(R.string.profile_contacts_added));
        addContactTitle.setTextColor(style.getTextPrimaryColor());
        removeContact.setOnClickListener(v -> {
            execute(ActorSDK.sharedActor().getMessenger().removeContact(user.getId()));
        });

        bind(user.isContact(), (isContact, valueModel) -> {
            if (isContact) {
                removeContact.setVisibility(View.VISIBLE);

                //fab
                fab.setImageResource(R.drawable.ic_message_white_24dp);
                fab.setOnClickListener(view -> startActivity(Intents.openPrivateDialog(user.getId(), true, getActivity())));
            } else {
                removeContact.setVisibility(View.GONE);

                //fab
                fab.setImageResource(R.drawable.ic_person_add_white_24dp);
                fab.setOnClickListener(view -> execute(ActorSDK.sharedActor().getMessenger().addContact(user.getId())));
            }
        });


        //
        // New Message
        //

        View newMessageView = res.findViewById(R.id.newMessage);
        ImageView newMessageIcon = (ImageView) newMessageView.findViewById(R.id.newMessageIcon);
        TextView newMessageTitle = (TextView) newMessageView.findViewById(R.id.newMessageText);
        {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_chat_black_24dp);
            drawable.mutate().setColorFilter(style.getSettingsIconColor(), PorterDuff.Mode.SRC_IN);
            newMessageIcon.setImageDrawable(drawable);
            newMessageTitle.setTextColor(style.getTextPrimaryColor());
        }
        newMessageView.setOnClickListener(v -> {
            startActivity(Intents.openPrivateDialog(user.getId(), true, getActivity()));
        });


        //
        // Voice Call
        //

        View voiceCallDivider = res.findViewById(R.id.voiceCallDivider);
        View voiceCallView = res.findViewById(R.id.voiceCall);
        if (ActorSDK.sharedActor().isCallsEnabled() && !user.isBot()) {
            ImageView voiceViewIcon = (ImageView) voiceCallView.findViewById(R.id.actionIcon);
            TextView voiceViewTitle = (TextView) voiceCallView.findViewById(R.id.actionText);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_phone_white_24dp);
            drawable.mutate().setColorFilter(style.getSettingsIconColor(), PorterDuff.Mode.SRC_IN);
            voiceViewIcon.setImageDrawable(drawable);
            voiceViewTitle.setTextColor(style.getTextPrimaryColor());

            voiceCallView.setOnClickListener(v -> {
                execute(ActorSDK.sharedActor().getMessenger().doCall(user.getId()));
            });
        } else {
            voiceCallView.setVisibility(View.GONE);
            voiceCallDivider.setVisibility(View.GONE);
        }

        //
        // Video Call
        //


        View videoCallDivider = res.findViewById(R.id.videoCallDivider);
        View videoCallView = res.findViewById(R.id.videoCall);
        if (ActorSDK.sharedActor().isCallsEnabled() && !user.isBot()) {
            ImageView voiceViewIcon = (ImageView) videoCallView.findViewById(R.id.videoCallIcon);
            TextView voiceViewTitle = (TextView) videoCallView.findViewById(R.id.videoCallText);
            Drawable drawable = getResources().getDrawable(R.drawable.ic_videocam_white_24dp);
            drawable.mutate().setColorFilter(style.getSettingsIconColor(), PorterDuff.Mode.SRC_IN);
            voiceViewIcon.setImageDrawable(drawable);
            voiceViewTitle.setTextColor(style.getTextPrimaryColor());

            videoCallView.setOnClickListener(v -> {
                execute(ActorSDK.sharedActor().getMessenger().doVideoCall(user.getId()));
            });
        } else {
            videoCallView.setVisibility(View.GONE);
            videoCallDivider.setVisibility(View.GONE);
        }


        //
        // Contact Information
        //

        final LinearLayout contactsContainer = (LinearLayout) res.findViewById(R.id.contactsContainer);

        String aboutString = user.getAbout().get();
        boolean isFirstContact = aboutString == null || aboutString.isEmpty();

        //
        // About
        //

        bind(user.getAbout(), new ValueChangedListener<String>() {
            private View userAboutRecord;

            @Override
            public void onChanged(final String newUserAbout, Value<String> valueModel) {
                if (newUserAbout != null && newUserAbout.length() > 0) {
                    if (userAboutRecord == null) {
                        userAboutRecord = buildRecordBig(newUserAbout,
                                R.drawable.ic_info_outline_black_24dp,
                                true,
                                false,
                                inflater, contactsContainer);
                    } else {
                        ((TextView) userAboutRecord.findViewById(R.id.value)).setText(newUserAbout);
                    }
                    if (recordFieldWithIcon != null) {
                        recordFieldWithIcon.findViewById(R.id.recordIcon).setVisibility(View.INVISIBLE);
                    }
                }
            }
        });


        if (!ActorSDK.sharedActor().isOnClientPrivacyEnabled() || user.isInPhoneBook().get()) {

            //
            // Phones
            //

            for (int i = 0; i < phones.size(); i++) {
                final UserPhone userPhone = phones.get(i);

                // Formatting Phone Number
                String _phoneNumber;
                try {
                    Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + userPhone.getPhone(), "us");
                    _phoneNumber = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                } catch (NumberParseException e) {
                    e.printStackTrace();
                    _phoneNumber = "+" + userPhone.getPhone();
                }
                final String phoneNumber = _phoneNumber;


                String phoneTitle = userPhone.getTitle();

                // "Mobile phone" is default value for non specified title
                // Trying to localize this
                if (phoneTitle.toLowerCase().equals("mobile phone")) {
                    phoneTitle = getString(R.string.settings_mobile_phone);
                }

                View view = buildRecord(phoneTitle,
                        phoneNumber,
                        R.drawable.ic_import_contacts_black_24dp,
                        isFirstContact,
                        false,
                        inflater, contactsContainer);
                if (isFirstContact) {
                    recordFieldWithIcon = view;
                }

                view.setOnClickListener(v -> {
                    new AlertDialog.Builder(getActivity())
                            .setItems(new CharSequence[]{
                                    getString(R.string.phone_menu_call).replace("{0}", phoneNumber),
                                    getString(R.string.phone_menu_sms).replace("{0}", phoneNumber),
                                    getString(R.string.phone_menu_share).replace("{0}", phoneNumber),
                                    getString(R.string.phone_menu_copy)
                            }, (dialog, which) -> {
                                if (which == 0) {
                                    startActivity(new Intent(Intent.ACTION_DIAL)
                                            .setData(Uri.parse("tel:+" + userPhone.getPhone())));
                                } else if (which == 1) {
                                    startActivity(new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse("sms:+" + userPhone.getPhone())));
                                } else if (which == 2) {
                                    startActivity(new Intent(Intent.ACTION_SEND)
                                            .setType("text/plain")
                                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text)
                                                    .replace("{0}", phoneNumber)
                                                    .replace("{1}", user.getName().get())));
                                } else if (which == 3) {
                                    ClipboardManager clipboard =
                                            (ClipboardManager) getActivity()
                                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Phone number", phoneNumber);
                                    clipboard.setPrimaryClip(clip);
                                    Snackbar.make(res, R.string.toast_phone_copied, Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(true);
                });

                view.setOnLongClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Phone number", "+" + userPhone.getPhone());
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(res, R.string.toast_phone_copied, Snackbar.LENGTH_SHORT)
                            .show();
                    return true;
                });

                isFirstContact = false;
            }

            //
            // Emails
            //

            for (int i = 0; i < emails.size(); i++) {
                final UserEmail userEmail = emails.get(i);
                View view = buildRecord(userEmail.getTitle(),
                        userEmail.getEmail(),
                        R.drawable.ic_import_contacts_black_24dp,
                        isFirstContact,
                        false,
                        inflater, contactsContainer);
                if (isFirstContact) {
                    recordFieldWithIcon = view;
                }

                view.setOnClickListener(v -> {
                    new AlertDialog.Builder(getActivity())
                            .setItems(new CharSequence[]{
                                    getString(R.string.email_menu_email).replace("{0}", userEmail.getEmail()),
                                    getString(R.string.phone_menu_copy)
                            }, (dialog, which) -> {
                                if (which == 0) {
                                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", userEmail.getEmail(), null)));
                                } else if (which == 1) {
                                    ClipboardManager clipboard =
                                            (ClipboardManager) getActivity()
                                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Email", userEmail.getEmail());
                                    clipboard.setPrimaryClip(clip);
                                    Snackbar.make(res, R.string.toast_email_copied, Snackbar.LENGTH_SHORT)
                                            .show();
                                }
                            })
                            .show()
                            .setCanceledOnTouchOutside(true);
                });
                view.setOnLongClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Email", "+" + userEmail.getEmail());
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(res, R.string.toast_email_copied, Snackbar.LENGTH_SHORT)
                            .show();
                    return true;
                });
                isFirstContact = false;
            }
        }


        //
        // Username
        //
        final boolean finalIsFirstContact = isFirstContact;
        bind(user.getNick(), new ValueChangedListener<String>() {
            private View userNameRecord;

            @Override
            public void onChanged(final String newUserName, Value<String> valueModel) {
                if (newUserName != null && newUserName.length() > 0) {
                    if (userNameRecord == null) {
                        userNameRecord = buildRecord(getString(R.string.nickname), "@" + newUserName,
                                R.drawable.ic_import_contacts_black_24dp,
                                finalIsFirstContact,
                                false,
                                inflater, contactsContainer);
                    } else {
                        ((TextView) userNameRecord.findViewById(R.id.value)).setText(newUserName);
                    }


                    userNameRecord.setOnLongClickListener(v -> {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Username", newUserName);
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(res, R.string.toast_nickname_copied, Snackbar.LENGTH_SHORT)
                                .show();
                        return true;
                    });

                    if (finalIsFirstContact) {
                        recordFieldWithIcon = userNameRecord;
                    }
                }
            }
        });


        //
        // Settings
        //
        {
            //
            // Notifications
            //
            View notificationContainer = res.findViewById(R.id.notificationsCont);
            View notificationPickerContainer = res.findViewById(R.id.notificationsPickerCont);

            ((TextView) notificationContainer.findViewById(R.id.settings_notifications_title)).setTextColor(style.getTextPrimaryColor());
            final SwitchCompat notificationEnable = (SwitchCompat) res.findViewById(R.id.enableNotifications);
            Peer peer = Peer.user(user.getId());
            notificationEnable.setChecked(messenger().isNotificationsEnabled(peer));
            if (messenger().isNotificationsEnabled(peer)) {
                ViewUtils.showView(notificationPickerContainer, false);
            } else {
                ViewUtils.goneView(notificationPickerContainer, false);
            }
            notificationEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                messenger().changeNotificationsEnabled(Peer.user(user.getId()), isChecked);

                if (isChecked) {
                    ViewUtils.showView(notificationPickerContainer, false);
                } else {
                    ViewUtils.goneView(notificationPickerContainer, false);
                }
            });
            notificationContainer.setOnClickListener(v -> notificationEnable.setChecked(!notificationEnable.isChecked()));
            ImageView iconView = (ImageView) res.findViewById(R.id.settings_notification_icon);
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            drawable.mutate();
            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
            iconView.setImageDrawable(drawable);

            ((TextView) notificationPickerContainer.findViewById(R.id.settings_notifications_picker_title)).setTextColor(style.getTextPrimaryColor());
            notificationPickerContainer.setOnClickListener(view -> {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                Uri currentSound = null;
                String defaultPath = null;
                Uri defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
                if (defaultUri != null) {
                    defaultPath = defaultUri.getPath();
                }

                String path = messenger().getPreferences().getString("userNotificationSound_" + uid);
                if (path == null) {
                    path = defaultPath;
                }
                if (path != null && !path.equals("none")) {
                    if (path.equals(defaultPath)) {
                        currentSound = defaultUri;
                    } else {
                        currentSound = Uri.parse(path);
                    }
                }
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentSound);
                startActivityForResult(intent, SOUND_PICKER_REQUEST_CODE);
            });

            //
            // Block
            //
            View blockContainer = res.findViewById(R.id.blockCont);
            final TextView blockTitle = (TextView) blockContainer.findViewById(R.id.settings_block_title);
            blockTitle.setTextColor(style.getTextPrimaryColor());
            bind(user.getIsBlocked(), (val, valueModel) -> {
                blockTitle.setText(val ? R.string.profile_settings_unblock : R.string.profile_settings_block);
            });
            blockContainer.setOnClickListener(v -> {
                if (!user.getIsBlocked().get()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.profile_settings_block_confirm).replace("{user}", user.getName().get()))
                            .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                                execute(messenger().blockUser(user.getId()));
                                dialog.dismiss();
                            })
                            .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                } else {
                    execute(messenger().unblockUser(user.getId()));
                }

            });
            ImageView blockIconView = (ImageView) res.findViewById(R.id.settings_block_icon);
            Drawable blockDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_block_white_24dp));
            drawable.mutate();
            DrawableCompat.setTint(blockDrawable, style.getSettingsIconColor());
            blockIconView.setImageDrawable(blockDrawable);
        }


        //
        // Scroll Coordinate
        //
        final ScrollView scrollView = ((ScrollView) res.findViewById(R.id.scrollContainer));
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> updateBar(scrollView.getScrollY()));
        updateBar(scrollView.getScrollY());

        return res;
    }

    private void checkInfiIcon() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == SOUND_PICKER_REQUEST_CODE) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringtone != null) {
                messenger().getPreferences().putString("userNotificationSound_" + uid, ringtone.toString());
            } else {
                messenger().getPreferences().putString("userNotificationSound_" + uid, "none");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        messenger().onProfileOpen(uid);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.edit) {
            startActivity(Intents.editUserName(uid, getActivity()));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateBar(int offset) {
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bar != null) {
            int fullColor = style.getToolBarColor();
            if (Math.abs(offset) > Screen.dp(248 - 56)) {
                bar.setBackgroundDrawable(new ColorDrawable(fullColor));
            } else {
                float alpha = Math.abs(offset) / (float) Screen.dp(248 - 56);
                bar.setBackgroundDrawable(new ColorDrawable(Color.argb(
                        (int) (255 * alpha),
                        Color.red(fullColor),
                        Color.green(fullColor),
                        Color.blue(fullColor)
                )));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        messenger().onProfileClosed(uid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}
