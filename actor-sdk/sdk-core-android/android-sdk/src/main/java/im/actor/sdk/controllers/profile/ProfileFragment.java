package im.actor.sdk.controllers.profile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.viewmodel.UserEmail;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ProfileFragment extends BaseFragment {

    public static final String EXTRA_UID = "uid";

    public static ProfileFragment create(int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        ProfileFragment res = new ProfileFragment();
        res.setArguments(bundle);
        return res;
    }

    private AvatarView avatarView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final UserVM user = users().get(getArguments().getInt(EXTRA_UID));
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
        avatarView.init(Screen.dp(96), 48);
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
        // Add/Remove Contact
        //

        final View addContact = res.findViewById(R.id.addContact);
        final ImageView addContactIcon = (ImageView) addContact.findViewById(R.id.addContactIcon);
        final TextView addContactTitle = (TextView) addContact.findViewById(R.id.addContactTitle);
        bind(user.isContact(), (isContact, valueModel) -> {
            if (isContact) {
                addContactTitle.setText(getString(R.string.profile_contacts_added));
                addContactTitle.setTextColor(style.getProfileContactIconColor());
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_check_circle_black_24dp));
                DrawableCompat.setTint(drawable, style.getProfileContactIconColor());
                addContactIcon.setImageDrawable(drawable);
                addContact.setOnClickListener(v -> {
                    execute(ActorSDK.sharedActor().getMessenger().removeContact(user.getId()));
                });
            } else {
                addContactTitle.setText(getString(R.string.profile_contacts_available));
                addContactTitle.setTextColor(style.getProfileContactIconColor());
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_person_add_white_24dp));
                DrawableCompat.setTint(drawable, style.getProfileContactIconColor());
                addContactIcon.setImageDrawable(drawable);
                addContact.setOnClickListener(v -> {
                    execute(ActorSDK.sharedActor().getMessenger().addContact(user.getId()));
                });
            }
        });


        //
        // New Message
        //

        View newMessageView = res.findViewById(R.id.newMessage);
        ImageView newMessageIcon = (ImageView) newMessageView.findViewById(R.id.newMessageIcon);
        TextView newMessageTitle = (TextView) newMessageView.findViewById(R.id.newMessageText);
        {
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_chat_black_24dp));
            DrawableCompat.setTint(drawable, style.getListActionColor());
            newMessageIcon.setImageDrawable(drawable);
            newMessageTitle.setTextColor(style.getListActionColor());
        }
        newMessageView.setOnClickListener(v -> {
            startActivity(Intents.openPrivateDialog(user.getId(), true, getActivity()));
        });


        //
        // Voice Call
        //

        View voiceCallView = res.findViewById(R.id.voiceCall);
        if (ActorSDK.sharedActor().isCallsEnabled()) {
            ImageView voiceViewIcon = (ImageView) voiceCallView.findViewById(R.id.actionIcon);
            TextView voiceViewTitle = (TextView) voiceCallView.findViewById(R.id.actionText);
            if (!user.isBot()) {
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_phone_white_24dp));
                DrawableCompat.setTint(drawable, style.getListActionColor());
                voiceViewIcon.setImageDrawable(drawable);
                voiceViewTitle.setTextColor(style.getListActionColor());

                voiceCallView.setOnClickListener(v -> {
                    execute(ActorSDK.sharedActor().getMessenger().doCall(user.getId()));
                });
            } else {
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_phone_white_24dp));
                DrawableCompat.setTint(drawable, style.getTextHintColor());
                voiceViewIcon.setImageDrawable(drawable);
                voiceViewTitle.setTextColor(style.getTextHintColor());
            }
        } else {
            voiceCallView.setVisibility(View.GONE);
        }


        //
        // Contact Information
        //

        final LinearLayout contactsContainer = (LinearLayout) res.findViewById(R.id.contactsContainer);
        boolean isFirstContact = true;

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
                    emails.size() == 0 && i == phones.size() - 1,
                    inflater, contactsContainer);


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
                    userName == null && i == emails.size() - 1,
                    inflater, contactsContainer);

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
                                true,
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
                }
            }
        });

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
                                true,
                                inflater, contactsContainer);
                    } else {
                        ((TextView) userAboutRecord.findViewById(R.id.value)).setText(newUserAbout);
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
            ((TextView) notificationContainer.findViewById(R.id.settings_notifications_title)).setTextColor(style.getTextPrimaryColor());
            final SwitchCompat notificationEnable = (SwitchCompat) res.findViewById(R.id.enableNotifications);
            notificationEnable.setChecked(messenger().isNotificationsEnabled(Peer.user(user.getId())));
            notificationEnable.setOnCheckedChangeListener((buttonView, isChecked) -> messenger().changeNotificationsEnabled(Peer.user(user.getId()), isChecked));
            notificationContainer.setOnClickListener(v -> notificationEnable.setChecked(!notificationEnable.isChecked()));
            ImageView iconView = (ImageView) res.findViewById(R.id.settings_notification_icon);
            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_list_black_24dp));
            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
            iconView.setImageDrawable(drawable);

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
            Drawable blockDrawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_block_white_18dp));
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
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}
