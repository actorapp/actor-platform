package im.actor.sdk.controllers.fragment.settings;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.core.viewmodel.generics.ArrayListUserPhone;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorSDKDelegate;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.controllers.fragment.help.HelpActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.CoverAvatarView;
import im.actor.sdk.view.TintImageView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public class MyProfileFragment extends BaseFragment {

    ActorSDKDelegate delegate;
    private int baseColor;
    private CoverAvatarView avatarView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        delegate = ActorSDK.sharedActor().getDelegate();
        baseColor = getResources().getColor(R.color.primary);
        ActorStyle style = ActorSDK.sharedActor().style;
        final UserVM userModel = users().get(myUid());

        final TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setTextColor(style.getProfileTitle());
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

        final LinearLayout nickContainer = (LinearLayout) view.findViewById(R.id.nickContainer);
        final LinearLayout contactsContainer = (LinearLayout) view.findViewById(R.id.phoneContainer);
        final FrameLayout about = (FrameLayout) view.findViewById(R.id.about);

        // TODO: Move bindings to onResume

        bind(userModel.getNick(), new ValueChangedListener<String>() {
            @Override
            public void onChanged(final String val, Value<String> Value) {
                final View recordView = inflater.inflate(R.layout.contact_record, nickContainer, false);
                TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
                tintImageView.setVisibility(View.INVISIBLE);
                String value = (val != null && !val.isEmpty()) ? val : getString(R.string.nickname_empty);
                String title = getString(R.string.nickname);

                ((TextView) recordView.findViewById(R.id.value)).setText(value);
                ((TextView) recordView.findViewById(R.id.title)).setText(title);
                nickContainer.removeAllViews();
                nickContainer.addView(recordView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Screen.dp(72)));

                recordView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().startActivity(Intents.editUserNick(getActivity()));
                    }
                });
            }
        });

        about.findViewById(R.id.title).setVisibility(View.GONE);
        about.findViewById(R.id.recordIcon).setVisibility(View.INVISIBLE);
        ((TextView) about.findViewById(R.id.value)).setText(getString(R.string.about_user_me));
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(Intents.editUserAbout(getActivity()));
            }
        });

        bind(userModel.getPhones(), new ValueChangedListener<ArrayListUserPhone>() {
            @Override
            public void onChanged(ArrayListUserPhone val, Value<ArrayListUserPhone> Value) {
                if (val.size() == 0) {
                    contactsContainer.setVisibility(View.GONE);
                } else {
                    contactsContainer.setVisibility(View.VISIBLE);
                    for (int i = 0; i < val.size(); i++) {
                        final UserPhone record = val.get(i);
                        View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                        TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
                        tintImageView.setTint(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());
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
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Phone number", "+" + record.getPhone());
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

        TextView settingsHeaderText = (TextView) view.findViewById(R.id.settings_header_text);
        settingsHeaderText.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());

        TextView settingsNotificationsTitle = (TextView) view.findViewById(R.id.settings_notifications_title);
        settingsNotificationsTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());

        TextView settingsChatTitle = (TextView) view.findViewById(R.id.settings_chat_title);
        settingsChatTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());

        TextView securityTitle = (TextView) view.findViewById(R.id.settings_security_title);
        securityTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());

        TintImageView securityIcon = (TintImageView) view.findViewById(R.id.settings_security_icon);
        securityIcon.setTint(style.getSettingsIcon());

        TextView helpTitle = (TextView) view.findViewById(R.id.settings_help_title);
        helpTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());

        TintImageView helpIcon = (TintImageView) view.findViewById(R.id.settings_help_icon);
        helpIcon.setTint(style.getSettingsIcon());

        TextView askTitle = (TextView) view.findViewById(R.id.settings_ask_title);
        askTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());

        TintImageView askIcon = (TintImageView) view.findViewById(R.id.settings_ask_icon);
        askIcon.setTint(style.getSettingsIcon());

        TintImageView notificationsSettingsIcon = (TintImageView) view.findViewById(R.id.settings_notification_icon);
        notificationsSettingsIcon.setTint(style.getSettingsIcon());

        TintImageView chatSettingsIcon = (TintImageView) view.findViewById(R.id.settings_chat_icon);
        chatSettingsIcon.setTint(style.getSettingsIcon());

        view.findViewById(R.id.after_phone_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackground());
        view.findViewById(R.id.bottom_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackground());

        if (delegate.getBeforeNickSettingsView(getActivity()) != null) {
            FrameLayout beforeNick = (FrameLayout) view.findViewById(R.id.before_nick_container);
            beforeNick.addView(delegate.getBeforeNickSettingsView(getActivity()), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (delegate.getBeforeNickSettingsView(getActivity()) != null) {
            FrameLayout afterPhone = (FrameLayout) view.findViewById(R.id.after_phone_container);
            afterPhone.addView(delegate.getAfterPhoneSettingsView(getActivity()), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (delegate.getSettingsTopView(getActivity()) != null) {
            FrameLayout settingsTop = (FrameLayout) view.findViewById(R.id.settings_top_container);
            settingsTop.addView(delegate.getSettingsTopView(getActivity()), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (delegate.getSettingsBottomView(getActivity()) != null) {
            FrameLayout settingsBot = (FrameLayout) view.findViewById(R.id.settings_bottom_container);
            settingsBot.addView(delegate.getSettingsBottomView(getActivity()), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        if (delegate.getBeforeSettingsCategories() != null) {
            LinearLayout beforeSettings = (LinearLayout) view.findViewById(R.id.before_settings_container);
            addCategories(beforeSettings, delegate.getBeforeSettingsCategories(), inflater);
        }

        if (delegate.getAfterSettingsCategories() != null) {
            LinearLayout afterSettings = (LinearLayout) view.findViewById(R.id.after_settings_container);
            addCategories(afterSettings, delegate.getAfterSettingsCategories(), inflater);
        }

        avatarView = (CoverAvatarView) view.findViewById(R.id.avatar);
        ImageView avatarBkgrnd = (ImageView) view.findViewById(R.id.avatar_bgrnd);
        avatarBkgrnd.setBackgroundColor(ActorSDK.sharedActor().style.getAvatarBackgroundColor());
        avatarView.setBkgrnd(avatarBkgrnd);

        bind(avatarView, users().get(myUid()).getAvatar());

        view.findViewById(R.id.avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ViewAvatarActivity.viewAvatar(myUid(), getActivity()));
            }
        });


        final ScrollView scrollView = ((ScrollView) view.findViewById(R.id.scrollContainer));
        scrollView.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackground());
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateActionBar(scrollView.getScrollY());
            }
        });

        updateActionBar(scrollView.getScrollY());

        return view;
    }

    private void addCategories(LinearLayout container, ActorSettingsCategory[] categories, LayoutInflater inflater) {
        Context context = getActivity();
        for (IActorSettingsCategory category : categories) {
            LinearLayout categoryContainer = (LinearLayout) inflater.inflate(R.layout.actor_settings_category, null);
            FrameLayout settingsContainer = (FrameLayout) categoryContainer.findViewById(R.id.settings_container);
            categoryContainer.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackground());
            TextView categoryName = (TextView) categoryContainer.findViewById(R.id.category_name);
            categoryName.setTextColor(ActorSDK.sharedActor().style.getSettingsMainTitle());
            categoryName.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());
            categoryName.setText(category.getCategoryName());
            if (category.getView(context) != null) {
                settingsContainer.addView(category.getView(getActivity()), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            } else if (category.getFields() != null) {
                addFields(settingsContainer, category.getFields(), inflater);
            }

            container.addView(categoryContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void addFields(FrameLayout container, ActorSettingsField[] fields, LayoutInflater inflater) {
        Context context = getActivity();
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        container.addView(ll, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        for (ActorSettingsField field : fields) {
            if (field.getView(context) != null) {
                View view = field.getView(context);
                ll.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                field.bindCreatedView(view);
            } else {
                LinearLayout fieldLayout = (LinearLayout) inflater.inflate(R.layout.actor_settings_field, null);
                TintImageView icon = (TintImageView) fieldLayout.findViewById(R.id.icon);
                icon.setTint(ActorSDK.sharedActor().style.getSettingsIcon());
                TextView name = (TextView) fieldLayout.findViewById(R.id.name);
                name.setTextColor(ActorSDK.sharedActor().style.getSettingsTitle());
                View rightView = field.getRightView(context);
                field.bindCreatedRightView(rightView);
                field.bindCreatedTextView(name);

                //Icon
                if (field.getIconResourceId() != 0) {
                    icon.setResource(field.getIconResourceId());
                    if (field.getIconColor() != -1) {
                        icon.setTint(field.getIconColor());
                    }
                } else {
                    icon.setVisibility(View.INVISIBLE);
                }
                //Name
                if (field.getName() != null) {
                    name.setText(field.getName());
                } else {
                    name.setVisibility(View.GONE);
                }
                //Right view
                if (rightView != null) {
                    fieldLayout.addView(rightView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                }
                //Click
                if (field.getOnclickListener() != null) {
                    fieldLayout.setOnClickListener(field.getOnclickListener());
                }
                //Field
                ll.addView(fieldLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }
            //Divider
            if (field.addBottomDivider()) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = Screen.dp(72);
                params.rightMargin = Screen.dp(16);
                View divider = inflater.inflate(R.layout.actor_settings_divider, null);
                ll.addView(divider, params);
            }
        }
    }

    private void updateActionBar(int offset) {

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
    public void onResume() {
        super.onResume();
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
