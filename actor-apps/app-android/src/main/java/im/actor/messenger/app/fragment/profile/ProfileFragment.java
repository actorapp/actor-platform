package im.actor.messenger.app.fragment.profile;

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestEditNickName;
import im.actor.core.api.rpc.ResponseSeq;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.media.DocumentsActivity;
import im.actor.messenger.app.fragment.preview.ViewAvatarActivity;
import im.actor.messenger.app.activity.BaseActivity;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.TintImageView;
import im.actor.core.entity.Peer;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.users;

/**
 * Created by ex3ndr on 12.09.14.
 */
public class ProfileFragment extends BaseFragment {

    private static final String EXTRA_UID = "uid";

    public static ProfileFragment create(int uid) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        ProfileFragment res = new ProfileFragment();
        res.setArguments(bundle);
        return res;
    }

    private int baseColor;

    private CoverAvatarView avatarView;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final int uid = getArguments().getInt(EXTRA_UID);
        final UserVM user = users().get(uid);
        final String nick = user.getNick().get();
        final String aboutText = user.getAbout().get();
        baseColor = getResources().getColor(R.color.primary);

        View res = inflater.inflate(R.layout.fragment_profile, container, false);

        bind((TextView) res.findViewById(R.id.name), user.getName());

        final TextView lastSeen = (TextView) res.findViewById(R.id.lastSeen);
        bind(lastSeen, lastSeen, user);

        final FrameLayout about = (FrameLayout) res.findViewById(R.id.about);
        if (aboutText != null && !aboutText.isEmpty()) {
            about.findViewById(R.id.title).setVisibility(View.GONE);
            about.findViewById(R.id.recordIcon).setVisibility(View.INVISIBLE);
            ((TextView) about.findViewById(R.id.value)).setText(getString(R.string.about_user));
            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new MaterialDialog.Builder(getActivity())
                            .title(getString(R.string.about_user))
                            .content(aboutText).show();

                }
            });
        } else {
            about.setVisibility(View.GONE);
        }


        final LinearLayout nickContainer = (LinearLayout) res.findViewById(R.id.nickContainer);
        if (nick != null && !nick.isEmpty()) {
            final View recordView = inflater.inflate(R.layout.contact_record, nickContainer, false);
            TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
            tintImageView.setVisibility(View.INVISIBLE);
            String value = nick;
            String title = getString(R.string.nickname);

            ((TextView) recordView.findViewById(R.id.value)).setText(value);
            ((TextView) recordView.findViewById(R.id.title)).setText(title);
            nickContainer.addView(recordView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Screen.dp(72)));

        }

        LinearLayout contactsContainer = (LinearLayout) res.findViewById(R.id.phoneContainer);
        View phonesDivider = res.findViewById(R.id.phoneDivider);
        if (user.getPhones().get().size() == 0) {
            contactsContainer.setVisibility(View.GONE);
            phonesDivider.setVisibility(View.GONE);
        } else {
            contactsContainer.setVisibility(View.VISIBLE);
            phonesDivider.setVisibility(View.VISIBLE);
            ArrayList<UserPhone> phones = user.getPhones().get();
            for (int i = 0; i < phones.size(); i++) {
                final UserPhone record = phones.get(i);
                View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
                if (i == 0) {
                    tintImageView.setResource(R.drawable.ic_call_white_36dp);
                    tintImageView.setVisibility(View.VISIBLE);
                } else {
                    tintImageView.setVisibility(View.INVISIBLE);
                }
                if (i != phones.size() - 1) {
                    recordView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                } else {
                    recordView.findViewById(R.id.divider).setVisibility(View.GONE);
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

                ((TextView) recordView.findViewById(R.id.value)).setText(phoneNumber);
                ((TextView) recordView.findViewById(R.id.title)).setText(record.getTitle());
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


        if (user.isBot()) {
            res.findViewById(R.id.profileAction).setVisibility(View.GONE);
        } else {
            res.findViewById(R.id.profileAction).setVisibility(View.VISIBLE);
            res.findViewById(R.id.profileAction).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intents.openPrivateDialog(uid, true, getActivity()));
                }
            });
        }

        avatarView = (CoverAvatarView) res.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) res.findViewById(R.id.avatar_bgrnd));

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

        View notificationContainter = res.findViewById(R.id.notificationsCont);
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

        final ScrollView scrollView = ((ScrollView) res.findViewById(R.id.scrollContainer));

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
