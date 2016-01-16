package im.actor.sdk.controllers.fragment.profile;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.viewmodel.UserEmail;
import im.actor.core.viewmodel.generics.ArrayListUserEmail;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.fragment.media.DocumentsActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.sdk.view.TintImageView;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class ProfileFragment extends BaseFragment {

    private static final String EXTRA_UID = "uid";
    private int baseColor;
    private CoverAvatarView avatarView;
    private boolean noEmails = false;
    private boolean noPhones = false;

    public ProfileFragment() {
    }

    public static ProfileFragment create(int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        ProfileFragment res = new ProfileFragment();
        res.setArguments(bundle);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int uid = getArguments().getInt(EXTRA_UID);
        final UserVM user = users().get(uid);
        final String nick = user.getNick().get();
        final String aboutText = user.getAbout().get();
        ActorStyle style = ActorSDK.sharedActor().style;

        View res = inflater.inflate(R.layout.fragment_profile, container, false);
        res.findViewById(R.id.container).setBackgroundColor(style.getMainBackgroundColor());
        TextView nameText = (TextView) res.findViewById(R.id.name);
        nameText.setTextColor(style.getProfileTitleColor());
        bind(nameText, user.getName());

        final TextView lastSeen = (TextView) res.findViewById(R.id.lastSeen);
        lastSeen.setTextColor(style.getProfileSubtitleColor());
        bind(lastSeen, lastSeen, user);

        boolean nickVisible = false;
        boolean aboutVivisbe = false;

        final FrameLayout about = (FrameLayout) res.findViewById(R.id.about);
        about.findViewById(R.id.divider).setBackgroundColor(style.getDividerColor());
        TextView aboutTitle = (TextView) about.findViewById(R.id.title);
        aboutTitle.setTextColor(style.getTextSecondaryColor());
        aboutTitle.setMaxLines(2);
        aboutTitle.setEllipsize(TextUtils.TruncateAt.END);
        TextView aboutValue = (TextView) about.findViewById(R.id.value);
        aboutValue.setTextColor(style.getTextPrimaryColor());
        TintImageView tintImageView = (TintImageView) about.findViewById(R.id.recordIcon);
        tintImageView.setTint(style.getRecordIconTintColor());
        tintImageView.setResource(R.drawable.ic_editor_format_quote_36dp);
        if (aboutText != null && !aboutText.isEmpty()) {
            aboutVivisbe = true;
            ((TextView) about.findViewById(R.id.value)).setText(getString(R.string.about_user));
            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.about_user)
                            .setMessage(aboutText)
                            .show();

                }
            });
        } else {
            about.setVisibility(View.GONE);
        }
        bind(aboutTitle, user.getAbout());


        final LinearLayout nickContainer = (LinearLayout) res.findViewById(R.id.nickContainer);
        if (nick != null && !nick.isEmpty()) {
            nickVisible = true;
            final View recordView = inflater.inflate(R.layout.contact_record, nickContainer, false);
            TextView nickValue = (TextView) recordView.findViewById(R.id.value);
            nickValue.setTextColor(style.getTextPrimaryColor());
            TextView nickTitle = (TextView) recordView.findViewById(R.id.title);
            nickTitle.setTextColor(style.getTextSecondaryColor());
            TintImageView nickIcon = (TintImageView) recordView.findViewById(R.id.recordIcon);
            nickIcon.setTint(style.getRecordIconTintColor());
            nickIcon.setResource(R.drawable.ic_star_white_36dp);
            String value = "@".concat(nick);
            String title = getString(R.string.nickname);
            recordView.findViewById(R.id.divider).setBackgroundColor(style.getDividerColor());
            ((TextView) recordView.findViewById(R.id.value)).setText(value);
            ((TextView) recordView.findViewById(R.id.title)).setText(title);
            nickContainer.addView(recordView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Screen.dp(72)));

        }

        LinearLayout contactsContainer = (LinearLayout) res.findViewById(R.id.phoneContainer);
        View aboveContactsDivider = null;
        if (aboutVivisbe) {
            aboveContactsDivider = about.findViewById(R.id.divider);
        } else if (nickVisible) {
            aboveContactsDivider = nickContainer.findViewById(R.id.divider);

        }

        if (user.getPhones().get().size() == 0) {
            noPhones = true;
        } else {
            contactsContainer.setVisibility(View.VISIBLE);
            ArrayList<UserPhone> phones = user.getPhones().get();
            for (int i = 0; i < phones.size(); i++) {
                final UserPhone record = phones.get(i);
                View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                TintImageView phoneIcon = (TintImageView) recordView.findViewById(R.id.recordIcon);
                phoneIcon.setTint(style.getRecordIconTintColor());
                if (i == 0) {
                    phoneIcon.setResource(R.drawable.ic_call_white_36dp);
                    phoneIcon.setVisibility(View.VISIBLE);
                } else {
                    phoneIcon.setVisibility(View.INVISIBLE);
                }
                View divider = recordView.findViewById(R.id.divider);
                divider.setBackgroundColor(style.getDividerColor());
                if (i != phones.size() - 1) {
                    divider.setVisibility(View.VISIBLE);
                } else {
                    divider.setVisibility(View.GONE);
                }

                String _phoneNumber;
                try {
                    Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + record.getPhone(), "us");
                    _phoneNumber = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                } catch (NumberParseException e) {
                    e.printStackTrace();
                    _phoneNumber = "+" + record.getPhone();
                }
                final String phoneNumber = _phoneNumber;

                TextView value = (TextView) recordView.findViewById(R.id.value);
                value.setTextColor(style.getTextPrimaryColor());
                value.setText(phoneNumber);
                TextView title = (TextView) recordView.findViewById(R.id.title);
                title.setTextColor(style.getTextSecondaryColor());
                title.setText(record.getTitle().replace("Mobile phone", getString(R.string.settings_mobile_phone)));
                contactsContainer.addView(recordView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Screen.dp(72)));

                recordView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity())
                                .setItems(new CharSequence[]{
                                        getString(R.string.phone_menu_call).replace("{0}", phoneNumber),
                                        getString(R.string.phone_menu_sms).replace("{0}", phoneNumber),
                                        getString(R.string.phone_menu_share).replace("{0}", phoneNumber),
                                        getString(R.string.phone_menu_copy)
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            startActivity(new Intent(Intent.ACTION_DIAL)
                                                    .setData(Uri.parse("tel:+" + record.getPhone())));
                                            // messenger().startCall(uid);
                                            // startActivity(new Intent(getActivity(), CallActivity.class));
                                        } else if (which == 1) {
                                            startActivity(new Intent(Intent.ACTION_VIEW)
                                                    .setData(Uri.parse("sms:+" + record.getPhone())));
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
                                            Toast.makeText(getActivity(), R.string.toast_phone_copied, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show()
                                .setCanceledOnTouchOutside(true);
                    }
                });
                recordView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Phone number", "+" + record.getPhone());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.toast_phone_copied, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        }

        if (user.getEmails().get().size() == 0) {
            noEmails = true;
        } else {
            ArrayList<UserEmail> emails = user.getEmails().get();

            contactsContainer.setVisibility(View.VISIBLE);
            for (int i = 0; i < emails.size(); i++) {
                final UserEmail record = emails.get(i);
                View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                TintImageView emailIcon = (TintImageView) recordView.findViewById(R.id.recordIcon);
                emailIcon.setTint(style.getSettingsCategoryTextColor());
                if (i == 0) {
                    emailIcon.setResource(R.drawable.ic_email_white_36dp);
                    emailIcon.setVisibility(View.VISIBLE);
                } else {
                    emailIcon.setVisibility(View.INVISIBLE);
                }

                View divider = recordView.findViewById(R.id.divider);
                if (i != emails.size() - 1) {
                    divider.setVisibility(View.VISIBLE);
                } else {
                    divider.setVisibility(View.GONE);
                }
                divider.setBackgroundColor(style.getDividerColor());

                final String email = record.getEmail();

                TextView value = (TextView) recordView.findViewById(R.id.value);
                value.setTextColor(style.getTextPrimaryColor());
                value.setText(email);
                TextView title = (TextView) recordView.findViewById(R.id.title);
                title.setTextColor(style.getTextSecondaryColor());
                title.setText(record.getTitle());
                contactsContainer.addView(recordView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Screen.dp(72)));

                recordView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity())
                                .setItems(new CharSequence[]{
                                        getString(R.string.email_menu_email).replace("{0}", email),
                                        getString(R.string.phone_menu_copy)
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", record.getEmail(), null)));
                                        } else if (which == 1) {
                                            ClipboardManager clipboard =
                                                    (ClipboardManager) getActivity()
                                                            .getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clip = ClipData.newPlainText("Email", email);
                                            clipboard.setPrimaryClip(clip);
                                            Toast.makeText(getActivity(), R.string.toast_email_copied, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .show()
                                .setCanceledOnTouchOutside(true);
                    }
                });
                recordView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Email", "+" + record.getEmail());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), R.string.toast_email_copied, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        }

        if (noEmails && noPhones) {
            contactsContainer.setVisibility(View.GONE);
            if (aboveContactsDivider != null) {
                aboveContactsDivider.setVisibility(View.GONE);
            } else {
                res.findViewById(R.id.phoneDivider).setVisibility(View.GONE);
            }
        }


        FloatingActionButton fab = (FloatingActionButton) res.findViewById(R.id.profileAction);
        fab.setColorNormal(style.getFabColor());
        fab.setColorPressed(style.getFabPressedColor());

        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intents.openPrivateDialog(uid, true, getActivity()));
            }
        });

        avatarView = (CoverAvatarView) res.findViewById(R.id.avatar);
        ImageView avatarBkgrnd = (ImageView) res.findViewById(R.id.avatar_bgrnd);
        if (style.getAvatarBackgroundResourse() != 0) {
            avatarBkgrnd.setImageResource(style.getAvatarBackgroundResourse());
        } else {
            avatarBkgrnd.setBackgroundColor(style.getAvatarBackgroundColor());
        }
        avatarView.setBkgrnd(avatarBkgrnd);

        bind(avatarView, user.getAvatar());
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ViewAvatarActivity.viewAvatar(uid, getActivity()));
            }
        });

        res.findViewById(R.id.docsContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DocumentsActivity.build(Peer.user(uid), getActivity()));
            }
        });

        res.findViewById(R.id.mediaContainer).setVisibility(View.GONE);
        ((TextView) res.findViewById(R.id.share_media_text)).setTextColor(style.getSettingsTitleColor());
        ((TextView) res.findViewById(R.id.mediaCount)).setTextColor(style.getTextHintColor());

        res.findViewById(R.id.docsContainer).setVisibility(View.GONE);
        ((TextView) res.findViewById(R.id.share_docs_title)).setTextColor(style.getSettingsTitleColor());
        ((TextView) res.findViewById(R.id.docCount)).setTextColor(style.getTextHintColor());

        View notificationContainter = res.findViewById(R.id.notificationsCont);
        ((TextView) notificationContainter.findViewById(R.id.settings_notifications_title)).setTextColor(style.getTextPrimaryColor());
        final SwitchCompat notificationEnable = (SwitchCompat) res.findViewById(R.id.enableNotifications);
        notificationEnable.setChecked(messenger().isNotificationsEnabled(Peer.user(uid)));
        notificationEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                messenger().changeNotificationsEnabled(Peer.user(uid), isChecked);
            }
        });
        notificationContainter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationEnable.setChecked(!notificationEnable.isChecked());
            }
        });

        TextView settingsHeaderText = (TextView) res.findViewById(R.id.settings_header_text);
        settingsHeaderText.setTextColor(style.getSettingsTitleColor());

        TextView sharedHeaderText = (TextView) res.findViewById(R.id.shared_header_text);
        sharedHeaderText.setTextColor(style.getSettingsTitleColor());

        TintImageView shareMediaIcon = (TintImageView) res.findViewById(R.id.share_media_icon);
        shareMediaIcon.setTint(style.getSettingsIconColor());

        TintImageView shareDocsIcon = (TintImageView) res.findViewById(R.id.share_docs_icon);
        shareDocsIcon.setTint(style.getSettingsIconColor());

        TintImageView notificationsSettingsIcon = (TintImageView) res.findViewById(R.id.settings_notification_icon);
        notificationsSettingsIcon.setTint(style.getSettingsIconColor());

        res.findViewById(R.id.phoneDivider).setBackgroundColor(style.getBackyardBackgroundColor());
        res.findViewById(R.id.after_shared_divider).setBackgroundColor(style.getBackyardBackgroundColor());
        res.findViewById(R.id.bottom_divider).setBackgroundColor(style.getBackyardBackgroundColor());

        final ScrollView scrollView = ((ScrollView) res.findViewById(R.id.scrollContainer));
        scrollView.setBackgroundColor(style.getBackyardBackgroundColor());
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateBar(scrollView.getScrollY());
            }
        });

        updateBar(scrollView.getScrollY());

        return res;
    }

    private void updateBar(int offset) {

        avatarView.setOffset(offset);

        ActionBar bar = ((BaseActivity) getActivity()).getSupportActionBar();
        int fullColor = baseColor;
        ActorStyle style = ActorSDK.sharedActor().style;
        if (style.getToolBarColor() != 0) {
            fullColor = style.getToolBarColor();
        }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarView != null) {
            avatarView.unbind();
            avatarView = null;
        }
    }
}
