package im.actor.messenger.app.fragment.settings;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.ViewAvatarActivity;
import im.actor.messenger.app.base.BaseActivity;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.fragment.help.HelpActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.messenger.app.view.TintImageView;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.mvvm.ValueChangedListener;
import im.actor.model.mvvm.ValueModel;
import im.actor.model.mvvm.generics.ArrayListUserPhone;
import im.actor.model.viewmodel.UserPhone;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;

/**
 * Created by ex3ndr on 09.09.14.
 */
public class MyProfileFragment extends BaseFragment {

    private int baseColor;

    private CoverAvatarView avatarView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        baseColor = getResources().getColor(R.color.primary);

        final UserVM userModel = users().get(myUid());

        final TextView nameView = (TextView) view.findViewById(R.id.name);

        bind(nameView, userModel.getName());

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

        final LinearLayout contactsContainer = (LinearLayout) view.findViewById(R.id.phoneContainer);

        // TODO: Move bindings to onResume
        bind(userModel.getPhones(), new ValueChangedListener<ArrayListUserPhone>() {
            @Override
            public void onChanged(ArrayListUserPhone val, ValueModel<ArrayListUserPhone> valueModel) {
                if (val.size() == 0) {
                    contactsContainer.setVisibility(View.GONE);
                } else {
                    contactsContainer.setVisibility(View.VISIBLE);
                    for (int i = 0; i < val.size(); i++) {
                        final UserPhone record = val.get(i);
                        View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                        TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
                        if (i == 0) {
                            tintImageView.setResource(R.drawable.ic_call_white_36dp);
                            tintImageView.setVisibility(View.VISIBLE);
                        } else {
                            tintImageView.setVisibility(View.INVISIBLE);
                        }
                        if (i != val.size() - 1) {
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
                                                } else if (which == 1) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW)
                                                            .setData(Uri.parse("sms:+" + record.getPhone())));
                                                } else if (which == 2) {
                                                    startActivity(new Intent(Intent.ACTION_SEND)
                                                            .setType("text/plain")
                                                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_share_text)
                                                                    .replace("{0}", phoneNumber)
                                                                    .replace("{1}", userModel.getName().get())));
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
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Phone number", "+" + record.getPhone());
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(getActivity(), R.string.toast_phone_copied, Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        });
                    }
                }
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
                startActivity(new Intent(getActivity(), SecuritySettingsActivity.class));
            }
        });

        view.findViewById(R.id.askQuestion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                execute(messenger().findUsers("75551234567"), R.string.progress_common, new CommandCallback<UserVM[]>() {
                    @Override
                    public void onResult(UserVM[] res) {
                        if (res.length >= 1) {
                            startActivity(Intents.openPrivateDialog(res[0].getId(), true, getActivity()));
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });

        avatarView = (CoverAvatarView) view.findViewById(R.id.avatar);
        avatarView.setBkgrnd((ImageView) view.findViewById(R.id.avatar_bgrnd));

        bind(avatarView, users().get(myUid()).getAvatar());

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
    public void onResume() {
        super.onResume();
        messenger().trackOwnProfileOpen();
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
        messenger().trackOwnProfileClosed();
        avatarView.unbind();
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
