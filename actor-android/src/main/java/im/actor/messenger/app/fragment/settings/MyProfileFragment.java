package im.actor.messenger.app.fragment.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.mvvm.ui.Listener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.*;
import im.actor.messenger.app.base.BaseBarActivity;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.scheme.avatar.Avatar;
import im.actor.messenger.util.Screen;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 09.09.14.
 */
public class MyProfileFragment extends BaseCompatFragment implements Listener<Avatar> {

    private int baseColor;

    private CoverAvatarView avatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        baseColor = getResources().getColor(R.color.primary);

        final UserModel userModel = users().get(myUid());

        final TextView nameView = (TextView) view.findViewById(R.id.name);

        getBinder().bindText(nameView, userModel.getNameModel());

        TextView phoneView = (TextView) view.findViewById(R.id.phone);
        try {
            Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + userModel.getPhone(), "us");
            phoneView.setText(PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));
        } catch (NumberParseException e) {
            e.printStackTrace();
            phoneView.setText("+" + userModel.getPhone());
        }

        view.findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NotificationsActivity.class));
            }
        });

        view.findViewById(R.id.helpSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        view.findViewById(R.id.profileAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber;
                String phoneNumber1;
                try {
                    Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + userModel.getPhone(), "us");
                    phoneNumber1 = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                } catch (NumberParseException e) {
                    e.printStackTrace();
                    phoneNumber1 = "+" + userModel.getPhone();
                }
                phoneNumber = phoneNumber1;

                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text)
                                        .replace("{0}", phoneNumber)),
                        getString(R.string.settings_share_profile_title)));
            }
        });

        view.findViewById(R.id.phoneContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber;
                String phoneNumber1;
                try {
                    Phonenumber.PhoneNumber number = PhoneNumberUtil.getInstance().parse("+" + userModel.getPhone(), "us");
                    phoneNumber1 = PhoneNumberUtil.getInstance().format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                } catch (NumberParseException e) {
                    e.printStackTrace();
                    phoneNumber1 = "+" + userModel.getPhone();
                }
                phoneNumber = phoneNumber1;

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
                                    startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:+" + userModel.getPhone())));
                                } else if (which == 1) {
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("sms:+" + userModel.getPhone())));
                                } else if (which == 2) {
                                    startActivity(new Intent(Intent.ACTION_SEND)
                                            .setType("text/plain")
                                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text)
                                                    .replace("{0}", phoneNumber)));
                                } else if (which == 3) {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Phone number", phoneNumber);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getActivity(), R.string.toast_phone_copied, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show()
                        .setCanceledOnTouchOutside(true);
            }
        });

        view.findViewById(R.id.chatSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatSettingsActivity.class));
            }
        });

        view.findViewById(R.id.encryption).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EncryptionActivity.class));
            }
        });

        avatar = (CoverAvatarView) view.findViewById(R.id.avatar);


        view.findViewById(R.id.avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ViewAvatarActivity.viewAvatar(myUid(), getActivity()));
            }
        });


        final ScrollView scrollView = ((ScrollView) view.findViewById(R.id.scrollContainer));

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateActionBar(scrollView.getScrollY());
            }
        });

        updateActionBar(scrollView.getScrollY());

        return view;
    }

    private void updateActionBar(int offset) {

        avatar.setOffset(offset);

        ActionBar bar = ((BaseBarActivity) getActivity()).getSupportActionBar();
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
    public void onResume() {
        super.onResume();
        bind(users().get(myUid()).getAvatar(), this);
    }


    @Override
    public void onUpdated(Avatar avatarb) {
        if (avatarb != null && avatarb.getSmallImage() != null) {
            avatar.request(avatarb);
        } else {
            avatar.clear();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.editProfile) {
            startActivity(Intents.editMyName(getActivity()));
            return true;
        } else if (item.getItemId() == R.id.changePhoto) {
            startActivity(ViewAvatarActivity.viewAvatar(myUid(), getActivity()));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        unbind(users().get(myUid()).getAvatar());
        avatar.clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatar != null) {
            avatar.close();
            avatar = null;
        }
    }
}
