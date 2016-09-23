package im.actor.sdk.controllers.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
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

import java.io.File;
import java.util.ArrayList;

import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserEmail;
import im.actor.core.viewmodel.UserPhone;
import im.actor.core.viewmodel.UserVM;
import im.actor.core.viewmodel.generics.ArrayListUserEmail;
import im.actor.core.viewmodel.generics.ArrayListUserPhone;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.BaseFragment;
import im.actor.sdk.controllers.fragment.help.HelpActivity;
import im.actor.sdk.controllers.fragment.preview.ViewAvatarActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.HeaderViewRecyclerAdapter;
import im.actor.sdk.view.avatar.AvatarView;
import im.actor.sdk.view.TintImageView;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ActorSDKMessenger.users;

public abstract class BaseActorSettingsFragment extends BaseFragment implements IActorSettingsFragment {

    private boolean animateToolbar = true;
    private int baseColor;
    private AvatarView avatarView;
    protected SharedPreferences shp;

    private boolean noPhones = false;
    private boolean noEmails = false;
    private HeaderViewRecyclerAdapter wallpaperAdapter;

    public BaseActorSettingsFragment() {
        setHasOptionsMenu(true);
    }

    public boolean isAnimateToolbar() {
        return animateToolbar;
    }

    public void setAnimateToolbar(boolean animateToolbar) {
        this.animateToolbar = animateToolbar;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        if (saveInstance != null) {
            animateToolbar = saveInstance.getBoolean("animateToolbar", true);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        messenger().onUserVisible(myUid());
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        shp = getActivity().getSharedPreferences("wallpaper", Context.MODE_PRIVATE);

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
        final LinearLayout about = (LinearLayout) view.findViewById(R.id.about);

        // TODO: Move bindings to onResume
        bind(userModel.getNick(), new ValueChangedListener<String>() {
            @Override
            public void onChanged(final String val, Value<String> Value) {
                final View recordView = inflater.inflate(R.layout.contact_record, nickContainer, false);
                ImageView nickIcon = (ImageView) recordView.findViewById(R.id.recordIcon);
                Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_mention_24_dp));
                drawable.mutate();
                DrawableCompat.setTint(drawable, style.getSettingsIconColor());
                nickIcon.setImageDrawable(drawable);

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

        final TextView aboutTitle = (TextView) about.findViewById(R.id.value);
        ImageView nickIcon = (ImageView) about.findViewById(R.id.recordIcon);
        Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_info_black_24dp));
        drawable.mutate();
        DrawableCompat.setTint(drawable, style.getSettingsIconColor());
        nickIcon.setImageDrawable(drawable);
        aboutTitle.setTextColor(style.getTextPrimaryColor());
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(Intents.editUserAbout(getActivity()));
            }
        });

        bind(userModel.getAbout(), new ValueChangedListener<String>() {
            @Override
            public void onChanged(String val, Value<String> valueModel) {
                if (val != null && !val.isEmpty()) {
                    aboutTitle.setTextColor(style.getTextPrimaryColor());
                    aboutTitle.setText(val);
                } else {
                    aboutTitle.setTextColor(style.getTextSecondaryColor());
                    aboutTitle.setText(getString(R.string.edit_about_edittext_hint));
                }
            }
        });

        bind(userModel.getPhones(), new ValueChangedListener<ArrayListUserPhone>() {
            @Override
            public void onChanged(ArrayListUserPhone val, Value<ArrayListUserPhone> Value) {
                if (val.size() == 0) {
                    noPhones = true;
                } else {
                    contactsContainer.setVisibility(View.VISIBLE);
                    for (int i = 0; i < val.size(); i++) {
                        final UserPhone record = val.get(i);
                        View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                        ImageView tintImageView = (ImageView) recordView.findViewById(R.id.recordIcon);
                        if (i == 0) {
                            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_phone_white_24dp));
                            drawable = drawable.mutate();
                            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
                            tintImageView.setImageDrawable(drawable);
                        } else {
                            tintImageView.setVisibility(View.INVISIBLE);
                        }

                        View divider = recordView.findViewById(R.id.divider);
                        if (i == val.size() - 1 && (userModel.getEmails().get() == null || userModel.getEmails().get().isEmpty())) {
                            divider.setVisibility(View.GONE);
                        } else {
                            divider.setVisibility(View.VISIBLE);
                        }
                        divider.setBackgroundColor(style.getDividerColor());

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

        bind(userModel.getEmails(), new ValueChangedListener<ArrayListUserEmail>() {
            @Override
            public void onChanged(ArrayListUserEmail val, Value<ArrayListUserEmail> Value) {
                if (val.size() == 0) {
                    noEmails = true;
                } else {
                    contactsContainer.setVisibility(View.VISIBLE);
                    for (int i = 0; i < val.size(); i++) {
                        final UserEmail record = val.get(i);
                        View recordView = inflater.inflate(R.layout.contact_record, contactsContainer, false);
                        ImageView tintImageView = (ImageView) recordView.findViewById(R.id.recordIcon);
                        if (i == 0) {
                            Drawable drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_email_white_24dp));
                            drawable.mutate();
                            DrawableCompat.setTint(drawable, style.getSettingsIconColor());
                            tintImageView.setImageDrawable(drawable);
                        } else {
                            tintImageView.setVisibility(View.INVISIBLE);
                        }

                        View divider = recordView.findViewById(R.id.divider);
                        if (i != val.size() - 1) {
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
                ActorSDK.sharedActor().startSecuritySettingsActivity(getActivity());
            }
        });

//        view.findViewById(R.id.encryption).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (AndroidLogProvider.isSendLogsEnabled()) {
//                    AndroidLogProvider.setSendLogs(null);
//                    Toast.makeText(getActivity(), "send logs off", Toast.LENGTH_LONG).show();
//                } else {
//
//                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
//                    builder.setTitle("Send logs integration url");
//
//                    LinearLayout ll = new LinearLayout(getActivity());
//                    ll.setPadding(Screen.dp(20), 0, Screen.dp(20), 0);
//
//                    final EditText input = new EditText(getActivity());
//                    ll.addView(input, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    builder.setView(ll);
//
//                    builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            AndroidLogProvider.setSendLogs(input.getText().toString());
//                            Toast.makeText(getActivity(), "send logs on", Toast.LENGTH_LONG).show();
//
//                        }
//                    });
//                    builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//
//                    builder.create().show();
//
//
//                }
//                return true;
//            }
//        });

        view.findViewById(R.id.blockedList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BlockedListActivity.class));
            }
        });

//        view.findViewById(R.id.chatSettings).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Toast.makeText(getActivity(), AndroidLogProvider.toggleWriteLogs() ? "write logs on" : "write logs off", Toast.LENGTH_LONG).show();
//                return true;
//            }
//        });

        View askQuestion = view.findViewById(R.id.askQuestion);

        if (ActorSDK.sharedActor().getHelpPhone() == null || ActorSDK.sharedActor().getHelpPhone().isEmpty()) {
            askQuestion.setVisibility(View.GONE);
            view.findViewById(R.id.divider3).setVisibility(View.GONE);
        }
        askQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                execute(messenger().findUsers(ActorSDK.sharedActor().getHelpPhone()), R.string.progress_common, new CommandCallback<UserVM[]>() {
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

        //Twitter
        View twitterView = view.findViewById(R.id.twitter);

        if (ActorSDK.sharedActor().getTwitterAcc() == null || ActorSDK.sharedActor().getTwitterAcc().isEmpty()) {
            twitterView.setVisibility(View.GONE);
        }
        twitterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + ActorSDK.sharedActor().getTwitterAcc()));
                startActivity(viewIntent);
            }
        });

        TextView twitterTitle = (TextView) view.findViewById(R.id.settings_twitter);
        twitterTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView twitterIcon = (TintImageView) view.findViewById(R.id.settings_twitter_icon);
        twitterIcon.setTint(style.getSettingsIconColor());

        //Home page
        View homePageView = view.findViewById(R.id.home_page);

        if (ActorSDK.sharedActor().getHomePage() == null || ActorSDK.sharedActor().getHomePage().isEmpty()) {
            homePageView.setVisibility(View.GONE);
        }
        homePageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ActorSDK.sharedActor().getHomePage()));
                startActivity(viewIntent);
            }
        });

        TextView homePageTitle = (TextView) view.findViewById(R.id.settings_home_page);
        homePageTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView homePageIcon = (TintImageView) view.findViewById(R.id.settings_home_page_icon);
        homePageIcon.setTint(style.getSettingsIconColor());

        TextView profileHeaderText = (TextView) view.findViewById(R.id.profile_info_header_text);
        profileHeaderText.setTextColor(style.getSettingsCategoryTextColor());

        TextView settingsHeaderText = (TextView) view.findViewById(R.id.settings_header_text);
        settingsHeaderText.setTextColor(style.getSettingsCategoryTextColor());

        TextView aboutHeaderText = (TextView) view.findViewById(R.id.about_header_text);
        aboutHeaderText.setTextColor(style.getSettingsCategoryTextColor());

        TextView settingsNotificationsTitle = (TextView) view.findViewById(R.id.settings_notifications_title);
        settingsNotificationsTitle.setTextColor(style.getSettingsTitleColor());

        TextView settingsChatTitle = (TextView) view.findViewById(R.id.settings_chat_title);
        settingsChatTitle.setTextColor(style.getSettingsTitleColor());

        TextView securityTitle = (TextView) view.findViewById(R.id.settings_security_title);
        securityTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView securityIcon = (TintImageView) view.findViewById(R.id.settings_security_icon);
        securityIcon.setTint(style.getSettingsIconColor());

        TextView blockedListTitle = (TextView) view.findViewById(R.id.settings_blocked_title);
        blockedListTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView blockedListIcon = (TintImageView) view.findViewById(R.id.settings_blocked_icon);
        blockedListIcon.setTint(style.getSettingsIconColor());

        TextView helpTitle = (TextView) view.findViewById(R.id.settings_help_title);
        helpTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView helpIcon = (TintImageView) view.findViewById(R.id.settings_help_icon);
        helpIcon.setTint(style.getSettingsIconColor());

        TextView askTitle = (TextView) view.findViewById(R.id.settings_ask_title);
        askTitle.setTextColor(style.getSettingsTitleColor());

        TintImageView askIcon = (TintImageView) view.findViewById(R.id.settings_ask_icon);
        askIcon.setTint(style.getSettingsIconColor());

        TintImageView notificationsSettingsIcon = (TintImageView) view.findViewById(R.id.settings_notification_icon);
        notificationsSettingsIcon.setTint(style.getSettingsIconColor());

        TintImageView chatSettingsIcon = (TintImageView) view.findViewById(R.id.settings_chat_icon);
        chatSettingsIcon.setTint(style.getSettingsIconColor());


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

        view.findViewById(R.id.avatarContainer).setBackgroundColor(style.getToolBarColor());

        avatarView = (AvatarView) view.findViewById(R.id.avatar);
        avatarView.init(Screen.dp(96), 44);
        avatarView.bind(users().get(myUid()));

        //Wallpaper
        if (showWallpaperCategory()) {
            ((TextView) view.findViewById(R.id.settings_wallpaper_title)).setTextColor(style.getSettingsCategoryTextColor());
            RecyclerView wallpapers = (RecyclerView) view.findViewById(R.id.wallpapers_list);
            wallpaperAdapter = new HeaderViewRecyclerAdapter(new WallpapersAdapter());
            FrameLayout fl = new FrameLayout(getActivity());
            ImageView icon = new ImageView(getActivity());
            icon.setImageResource(R.drawable.ic_image_black_24dp);
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setColorFilter(ActorSDK.sharedActor().style.getSettingsIconColor(), PorterDuff.Mode.SRC_IN);
            fl.addView(icon, new FrameLayout.LayoutParams(Screen.dp(72), Screen.dp(85), Gravity.CENTER));
            fl.setLayoutParams(new ViewGroup.LayoutParams(Screen.dp(72), Screen.dp(85)));
            wallpaperAdapter.addHeaderView(fl);
            wallpapers.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
            wallpapers.setAdapter(wallpaperAdapter);
        } else {
            view.findViewById(R.id.wallpapers_list).setVisibility(View.GONE);
            view.findViewById(R.id.settings_wallpaper_title).setVisibility(View.GONE);
        }

        view.findViewById(R.id.avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ViewAvatarActivity.viewAvatar(myUid(), getActivity()));
            }
        });


        final ScrollView scrollView = ((ScrollView) view.findViewById(R.id.scrollContainer));
        scrollView.setBackgroundColor(style.getMainBackgroundColor());
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                updateActionBar(scrollView.getScrollY());
            }
        });

        updateActionBar(scrollView.getScrollY());

        return view;
    }

    private void addCategories(LinearLayout container, ActorSettingsCategories categories, LayoutInflater inflater) {
        for (ActorSettingsCategory category : categories) {
            LinearLayout categoryContainer = (LinearLayout) inflater.inflate(R.layout.actor_settings_category, null);
            FrameLayout settingsContainer = (FrameLayout) categoryContainer.findViewById(R.id.settings_container);
            TextView categoryName = (TextView) categoryContainer.findViewById(R.id.category_name);
            categoryName.setTextColor(ActorSDK.sharedActor().style.getSettingsCategoryTextColor());

            //Icon
            TintImageView icon = (TintImageView) categoryContainer.findViewById(R.id.icon);
            icon.setTint(ActorSDK.sharedActor().style.getSettingsIconColor());
            if (category.getIconResourceId() != 0) {
                icon.setResource(category.getIconResourceId());
                if (category.getIconColor() != -1) {
                    icon.setTint(category.getIconColor());
                }
            } else {
                icon.setVisibility(View.INVISIBLE);
            }
            categoryName.setText(category.getText());
            if (category.getFields() != null) {
                addFields(settingsContainer, category.getFields(), inflater);
            }

            container.addView(categoryContainer, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void addFields(FrameLayout container, ArrayList<ActorSettingsField> fields, LayoutInflater inflater) {
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
                    fieldLayout.addView(rightView, field.getRightViewWidth(), field.getRightViewHeight());
                }
                //Click
                if (getMenuFieldOnClickListener() != null) {
                    fieldLayout.setId(field.getId());
                    fieldLayout.setOnClickListener(getMenuFieldOnClickListener());
                }
                //Field
                ll.addView(fieldLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            }

        }
    }

    private void updateActionBar(int offset) {
        if (!animateToolbar) {
            return;
        }
        Activity activity = getActivity();
        if (!(activity instanceof BaseActivity)) {
            return;
        }
        ActionBar bar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (bar == null) {
            return;
        }
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

    public static String getWallpaperFile() {
        File externalFile = messenger().getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/actor/wallpapers/");
        dest.mkdirs();

        File outputFile = new File(dest, "customWallpaper" + ".jpg");
        return outputFile.getAbsolutePath();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wallpaperAdapter != null) {
            ((WallpapersAdapter) wallpaperAdapter.getWrappedAdapter()).setSelected(getActivity().getSharedPreferences("wallpaper", Context.MODE_PRIVATE).getInt("wallpaper", 0));
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("animateToolbar", animateToolbar);
    }

    @Override
    public View getBeforeNickSettingsView() {
        return null;
    }

    @Override
    public View getAfterPhoneSettingsView() {
        return null;
    }

    @Override
    public View getSettingsTopView() {
        return null;
    }

    @Override
    public View getSettingsBottomView() {
        return null;
    }

    @Override
    public boolean showWallpaperCategory() {
        return true;
    }

    @Override
    public boolean showAskQuestion() {
        return true;
    }

    public abstract View.OnClickListener getMenuFieldOnClickListener();

    @Override
    public ActorSettingsCategories getBeforeSettingsCategories() {
        return null;
    }

    @Override
    public ActorSettingsCategories getAfterSettingsCategories() {
        return null;
    }
}
