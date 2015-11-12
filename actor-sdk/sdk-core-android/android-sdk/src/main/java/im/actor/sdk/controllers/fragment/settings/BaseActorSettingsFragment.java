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

public abstract class BaseActorSettingsFragment extends BaseFragment implements IActorSettingsFragment {

    private int baseColor;
    private CoverAvatarView avatarView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        baseColor = getResources().getColor(R.color.primary);
        final ActorStyle style = ActorSDK.sharedActor().style;
        final UserVM userModel = users().get(myUid());

        final TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setShadowLayer(1, 1, 1, style.getDividerColor());
        nameView.setTextColor(style.getProfileTitleColor());
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
                recordView.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
                TintImageView tintImageView = (TintImageView) recordView.findViewById(R.id.recordIcon);
                tintImageView.setVisibility(View.INVISIBLE);
                String value = (val != null && !val.isEmpty()) ? val : getString(R.string.nickname_empty);
                String title = getString(R.string.nickname);

                TextView nickValue = (TextView) recordView.findViewById(R.id.value);
                nickValue.setText(value);
                nickValue.setTextColor(style.getTextPrimaryColor());
                TextView nickTitle = (TextView) recordView.findViewById(R.id.title);
                nickTitle.setText(title);
                nickTitle.setTextColor(style.getTextSecondaryColor());
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

        TextView aboutTitle = (TextView) about.findViewById(R.id.title);
        aboutTitle.setTextColor(style.getTextSecondaryColor());
        aboutTitle.setVisibility(View.GONE);
        about.findViewById(R.id.recordIcon).setVisibility(View.INVISIBLE);
        TextView aboutValue = (TextView) about.findViewById(R.id.value);
        aboutValue.setTextColor(style.getTextPrimaryColor());
        aboutValue.setText(getString(R.string.about_user_me));
        about.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
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

                        View divider = recordView.findViewById(R.id.divider);
                        if (i != val.size() - 1) {
                            divider.setVisibility(View.VISIBLE);
                        } else {
                            divider.setVisibility(View.GONE);
                        }
                        divider.setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

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
                        title.setText(record.getTitle());
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
        settingsNotificationsTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());

        TextView settingsChatTitle = (TextView) view.findViewById(R.id.settings_chat_title);
        settingsChatTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());

        TextView securityTitle = (TextView) view.findViewById(R.id.settings_security_title);
        securityTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());

        TintImageView securityIcon = (TintImageView) view.findViewById(R.id.settings_security_icon);
        securityIcon.setTint(style.getSettingsIconColor());

        TextView helpTitle = (TextView) view.findViewById(R.id.settings_help_title);
        helpTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());

        TintImageView helpIcon = (TintImageView) view.findViewById(R.id.settings_help_icon);
        helpIcon.setTint(style.getSettingsIconColor());

        TextView askTitle = (TextView) view.findViewById(R.id.settings_ask_title);
        askTitle.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());

        TintImageView askIcon = (TintImageView) view.findViewById(R.id.settings_ask_icon);
        askIcon.setTint(style.getSettingsIconColor());

        TintImageView notificationsSettingsIcon = (TintImageView) view.findViewById(R.id.settings_notification_icon);
        notificationsSettingsIcon.setTint(style.getSettingsIconColor());

        TintImageView chatSettingsIcon = (TintImageView) view.findViewById(R.id.settings_chat_icon);
        chatSettingsIcon.setTint(style.getSettingsIconColor());

        view.findViewById(R.id.after_phone_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        view.findViewById(R.id.bottom_divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());

        view.findViewById(R.id.divider1).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        view.findViewById(R.id.divider2).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        view.findViewById(R.id.divider3).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        view.findViewById(R.id.divider4).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        if (getBeforeNickSettingsView() != null) {
            FrameLayout beforeNick = (FrameLayout) view.findViewById(R.id.before_nick_container);
            beforeNick.addView(getBeforeNickSettingsView(), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (getBeforeNickSettingsView() != null) {
            FrameLayout afterPhone = (FrameLayout) view.findViewById(R.id.after_phone_container);
            afterPhone.addView(getAfterPhoneSettingsView(), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (getSettingsTopView() != null) {
            FrameLayout settingsTop = (FrameLayout) view.findViewById(R.id.settings_top_container);
            settingsTop.addView(getSettingsTopView(), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        if (getSettingsBottomView() != null) {
            FrameLayout settingsBot = (FrameLayout) view.findViewById(R.id.settings_bottom_container);
            settingsBot.addView(getSettingsBottomView(), FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        if (getBeforeSettingsCategories() != null) {
            LinearLayout beforeSettings = (LinearLayout) view.findViewById(R.id.before_settings_container);
            addCategories(beforeSettings, getBeforeSettingsCategories(), inflater);
        }

        if (getAfterSettingsCategories() != null) {
            LinearLayout afterSettings = (LinearLayout) view.findViewById(R.id.after_settings_container);
            addCategories(afterSettings, getAfterSettingsCategories(), inflater);
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
        scrollView.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
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
            categoryContainer.findViewById(R.id.divider).setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
            TextView categoryName = (TextView) categoryContainer.findViewById(R.id.category_name);
            categoryName.setTextColor(ActorSDK.sharedActor().style.getSettingsMainTitleColor());
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
            if (field.getView() != null) {
                View view = field.getView();
                ll.addView(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            } else {
                LinearLayout fieldLayout = (LinearLayout) inflater.inflate(R.layout.actor_settings_field, null);
                TintImageView icon = (TintImageView) fieldLayout.findViewById(R.id.icon);
                icon.setTint(ActorSDK.sharedActor().style.getSettingsIconColor());
                TextView name = (TextView) fieldLayout.findViewById(R.id.name);
                name.setTextColor(ActorSDK.sharedActor().style.getSettingsTitleColor());
                View rightView = field.getRightView();

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
                divider.setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
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
