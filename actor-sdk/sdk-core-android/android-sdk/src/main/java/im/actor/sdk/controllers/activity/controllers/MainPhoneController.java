package im.actor.sdk.controllers.activity.controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.SearchEntity;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.controllers.activity.ActorMainActivity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.activity.AddContactActivity;
import im.actor.sdk.controllers.compose.ComposeActivity;
import im.actor.sdk.controllers.compose.CreateGroupActivity;
import im.actor.sdk.controllers.contacts.ContactsFragment;
import im.actor.sdk.controllers.dialogs.DialogsFragment;
import im.actor.sdk.controllers.fragment.help.HelpActivity;
import im.actor.sdk.controllers.fragment.main.SearchAdapter;
import im.actor.sdk.util.Screen;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.adapters.FragmentNoMenuStatePagerAdapter;
import im.actor.sdk.view.adapters.HeaderViewRecyclerAdapter;
import im.actor.sdk.view.adapters.OnItemClickedListener;
import im.actor.sdk.view.PagerSlidingTabStrip;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.showView;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MainPhoneController extends MainBaseController {

    protected ViewPager pager;

    private HomePagerAdapter homePagerAdapter;

    private RecyclerView searchList;
    private View searchContainer;
    private TextView searchEmptyView;
    private TextView searchHintView;

    private boolean isSearchVisible = false;
    private SearchAdapter searchAdapter;
    private BindedDisplayList<SearchEntity> searchDisplay;
    private final DisplayList.Listener searchListener = new DisplayList.Listener() {
        @Override
        public void onCollectionChanged() {
            onSearchChanged();
        }
    };
    private SearchView searchView;
    private MenuItem searchMenu;

    protected PagerSlidingTabStrip barTabs;

    private View syncInProgressView;
    private View emptyContactsView;

    private com.getbase.floatingactionbutton.FloatingActionButton fabRoot;

    private String joinGroupUrl;
    private String sendUriString = "";
    private String sendText = "";
    private ArrayList<String> sendUriMultiple = new ArrayList<String>();
    private int shareUser;
    private String forwardText = "";
    private String forwardTextRaw = "";
    private byte[] docContent = null;

    public MainPhoneController(ActorMainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onDialogClicked(final Dialog item) {
        if ((sendUriMultiple != null && !sendUriMultiple.isEmpty()) || docContent != null || (sendUriString != null && !sendUriString.isEmpty())) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getActivity().getString(R.string.confirm_share) + " " + item.getDialogTitle() + "?")
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openDialog(item);
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            openDialog(item);
        }
    }

    @Override
    public void onContactClicked(final Contact contact) {
        if ((sendUriMultiple != null && !sendUriMultiple.isEmpty()) || docContent != null || (sendUriString != null && !sendUriString.isEmpty())) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getActivity().getString(R.string.confirm_share) + " " + contact.getName() + "?")
                    .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openContactDialog(contact);
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            openContactDialog(contact);
        }
    }

    private void openDialog(Dialog item) {
        startActivity(Intents.openDialog(item.getPeer(), false, getActivity()).putExtra("send_uri", sendUriString)
                .putExtra("send_uri_multiple", sendUriMultiple)
                .putExtra("send_text", sendText)
                .putExtra("forward_text", forwardText)
                .putExtra("forward_text_raw", forwardTextRaw)
                .putExtra("forward_content", docContent)
                .putExtra("share_user", shareUser));
        clearShare();
    }

    private void openContactDialog(Contact contact) {
        getActivity().startActivity(Intents.openPrivateDialog(contact.getUid(), true, getActivity()).putExtra("send_uri", sendUriString)
                .putExtra("send_uri_multiple", sendUriMultiple)
                .putExtra("send_text", sendText)
                .putExtra("forward_text", forwardText)
                .putExtra("forward_text_raw", forwardTextRaw)
                .putExtra("forward_content", docContent)
                .putExtra("share_user", shareUser));

        clearShare();
    }

    private void clearShare() {
        sendUriMultiple.clear();
        sendUriString = "";
        docContent = null;
        forwardText = "";
        forwardTextRaw = "";
        sendText = "";
        shareUser = 0;
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        Intent intent = getIntent();

        handleIntent(intent);

        setContentView(R.layout.actor_activity_main);
        ActorStyle style = ActorSDK.sharedActor().style;
        getActivity().setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));

        syncInProgressView = findViewById(R.id.syncInProgress);
        ((TextView) syncInProgressView.findViewById(R.id.wait_text)).setTextColor(style.getTextSecondaryColor());
        ((TextView) syncInProgressView.findViewById(R.id.sync_text)).setTextColor(style.getMainColor());
        syncInProgressView.findViewById(R.id.sync_background).setBackgroundColor(style.getMainBackgroundColor());
        syncInProgressView.findViewById(R.id.syncInProgress).setBackgroundColor(style.getMainBackgroundColor());
        emptyContactsView = findViewById(R.id.emptyContacts);
        findViewById(R.id.emptyContactsFrame).setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        ((TextView) emptyContactsView.findViewById(R.id.no_contacts)).setTextColor(style.getTextSecondaryColor());
        ((TextView) emptyContactsView.findViewById(R.id.no_contacts)).setText(getResources().getString(R.string.main_empty_invite_hint).replace("{appName}", ActorSDK.sharedActor().getAppName()));
        ((TextView) emptyContactsView.findViewById(R.id.add_contact_manually_text)).setTextColor(style.getTextSecondaryColor());
        ((TextView) emptyContactsView.findViewById(R.id.empty_contacts_text)).setTextColor(style.getMainColor());
        emptyContactsView.findViewById(R.id.empty_contacts_bg).setBackgroundColor(style.getMainColor());

        TextView addContactBtnText = (TextView) findViewById(R.id.addContactButtonText);
        addContactBtnText.setTextColor(style.getTextSecondaryColor());
        addContactBtnText.setTypeface(Fonts.medium());
        TextView inviteBtnText = (TextView) findViewById(R.id.inviteButtonText);
        inviteBtnText.setTypeface(Fonts.medium());
        inviteBtnText.setTextColor(style.getTextPrimaryInvColor());

        fabRoot = (FloatingActionButton) findViewById(R.id.rootFab);
        if (ActorSDK.sharedActor().style.getFabColor() != 0) {
            fabRoot.setColorNormal(ActorSDK.sharedActor().style.getFabColor());
        }
        if (ActorSDK.sharedActor().style.getFabPressedColor() != 0) {
            fabRoot.setColorPressed(ActorSDK.sharedActor().style.getFabPressedColor());
        }
        fabRoot.setOnClickListener(v -> startActivity(new Intent(getActivity(), ComposeActivity.class)));

        searchList = (RecyclerView) findViewById(R.id.searchList);
        searchList.setLayoutManager(new ChatLinearLayoutManager(getActivity()));

        searchContainer = findViewById(R.id.searchCont);
        searchContainer.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        searchEmptyView = (TextView) findViewById(R.id.empty);
        searchHintView = (TextView) findViewById(R.id.searchHint);
        searchEmptyView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setVisibility(View.GONE);
        searchEmptyView.setVisibility(View.GONE);

        pager = (ViewPager) findViewById(R.id.vp_pager);
        pager.setOffscreenPageLimit(2);
        homePagerAdapter = getHomePagerAdapter();
        pager.setAdapter(homePagerAdapter);
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int prevPage = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (position != prevPage) {
                    }
                    if (prevPage == 1) {
                    }
                    prevPage = position;
                } else if (position == 1) {
                    if (position != prevPage) {
                    }
                    if (prevPage == 0) {
                    }
                    prevPage = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        findViewById(R.id.addContactButton).setOnClickListener(v -> startActivity(new Intent(getActivity(), AddContactActivity.class)));

        findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inviteMessage = getResources().getString(R.string.invite_message).replace("{inviteUrl}", ActorSDK.sharedActor().getInviteUrl()).replace("{appName}", ActorSDK.sharedActor().getAppName());
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMessage);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    @NonNull
    public HomePagerAdapter getHomePagerAdapter() {
        return new HomePagerAdapter(getFragmentManager());
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null) {
                    joinGroupUrl = intent.getData().toString();
                } else if (intent.getAction().equals(Intent.ACTION_SEND)) {
                    if ("text/plain".equals(getIntent().getType())) {
                        sendText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    } else if (intent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                        sendUriString = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
                    }
                } else if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (imageUris != null) {
                        for (Uri u : imageUris) {
                            sendUriMultiple.add(u.toString());
                        }
                    }
                }
            }

            if (intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                if (extras.containsKey("share_user")) {
                    shareUser = extras.getInt("share_user");
                } else if (extras.containsKey("forward_text")) {
                    forwardText = extras.getString("forward_text");
                    forwardTextRaw = extras.getString("forward_text_raw");
                } else if (extras.containsKey("forward_content")) {
                    docContent = extras.getByteArray("forward_content");
                }
            }
        }
    }

    @Override
    public void onResume() {
        ActionBar ab = getActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);

        if (ActorSDK.sharedActor().style.getToolBarColor() != 0) {
            ab.setBackgroundDrawable(new ColorDrawable(ActorSDK.sharedActor().style.getToolBarColor()));
        }

        onConfigireToolbarCustomView(ab);

        onShowToolbarCustomView();
        emptyContactsView.setVisibility(View.GONE);
        syncInProgressView.setVisibility(View.GONE);

        getActivity().bind(messenger().getAppState().getIsAppLoaded(),
                messenger().getAppState().getIsAppEmpty(),
                new ValueDoubleChangedListener<Boolean, Boolean>() {
                    @Override
                    public void onChanged(Boolean isAppLoaded, Value<Boolean> Value,
                                          Boolean isAppEmpty, Value<Boolean> Value2) {
                        if (isAppEmpty) {
                            if (isAppLoaded) {
                                onHideToolbarCustomView();
                                emptyContactsView.setVisibility(View.VISIBLE);
                                syncInProgressView.setVisibility(View.GONE);
                                getActivity().invalidateOptionsMenu();
                            } else {
                                onHideToolbarCustomView();
                                emptyContactsView.setVisibility(View.GONE);
                                syncInProgressView.setVisibility(View.VISIBLE);
                                getActivity().invalidateOptionsMenu();
                            }
                        } else {
                            onShowToolbarCustomView();
                            emptyContactsView.setVisibility(View.GONE);
                            syncInProgressView.setVisibility(View.GONE);
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });
    }

    protected void onShowToolbarCustomView() {
        barTabs.setVisibility(View.VISIBLE);
    }

    protected void onHideToolbarCustomView() {
        barTabs.setVisibility(View.GONE);
    }

    protected void onConfigireToolbarCustomView(ActionBar ab) {
        FrameLayout tabsContainer = new FrameLayout(getActivity());
        barTabs = new PagerSlidingTabStrip(getActivity());
        barTabs.setTabBackground(R.drawable.selector_bar);
        //barTabs.setIndicatorColorResource(R.color.main_tab_selected);
        barTabs.setIndicatorHeight(Screen.dp(2));

        barTabs.setDividerColorResource(android.R.color.transparent);
        //barTabs.setTextColorResource(R.color.main_tab_text);
        barTabs.setTextSize(Screen.dp(14));
        barTabs.setUnderlineHeight(0);

        barTabs.setViewPager(pager);

        // Icons
        // int width = Screen.dp(72 * 2);

        tabsContainer.addView(barTabs, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Screen.dp(56)));
        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Screen.dp(56));
        tabsContainer.setLayoutParams(lp);
        ab.setCustomView(tabsContainer);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        searchMenu = menu.findItem(R.id.search);
        if (messenger().getAppState().getIsAppEmpty().get()) {
            searchMenu.setVisible(false);
        } else {
            searchMenu.setVisible(true);
        }
        searchView = (SearchView) searchMenu.getActionView();
        searchView.setIconifiedByDefault(true);

        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                showSearch();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideSearch();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (isSearchVisible) {
                    if (s.trim().length() > 0) {
                        searchDisplay.initSearch(s.trim().toLowerCase(), false);
                        searchAdapter.setQuery(s.trim().toLowerCase());
                    } else {
                        searchDisplay.initEmpty();
                    }
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (isSearchVisible) {
            hideSearch();
            return true;
        }

        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        hideSearch();
    }

    private void showSearch() {
        if (isSearchVisible) {
            return;
        }
        isSearchVisible = true;

        searchDisplay = messenger().buildSearchDisplayList();
        searchAdapter = new SearchAdapter(getActivity(), searchDisplay, new OnItemClickedListener<SearchEntity>() {
            @Override
            public void onClicked(SearchEntity item) {
                startActivity(Intents.openDialog(item.getPeer(), false, getActivity()));
            }

            @Override
            public boolean onLongClicked(SearchEntity item) {
                return false;
            }
        });
        HeaderViewRecyclerAdapter recyclerAdapter = new HeaderViewRecyclerAdapter(searchAdapter);

        View header = new View(getActivity());
        header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(0)));
        header.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        recyclerAdapter.addHeaderView(header);

        searchList.setAdapter(recyclerAdapter);
        searchDisplay.addListener(searchListener);
        onHideToolbarCustomView();
        showView(searchHintView, false);
        goneView(searchEmptyView, false);

        showView(searchContainer);
    }

    private void onSearchChanged() {
        if (!searchDisplay.isInSearchState()) {
            showView(searchHintView);
            goneView(searchEmptyView);
        } else {
            goneView(searchHintView);
            if (searchDisplay.getSize() == 0) {
                showView(searchEmptyView);
            } else {
                goneView(searchEmptyView);
            }
        }
    }

    private void hideSearch() {
        if (!isSearchVisible) {
            return;
        }
        isSearchVisible = false;

        if (searchDisplay != null) {
            searchDisplay.dispose();
            searchDisplay = null;
        }
        searchAdapter = null;
        searchList.setAdapter(null);

        goneView(searchContainer);
        onShowToolbarCustomView();
        if (searchMenu != null) {
            if (searchMenu.isActionViewExpanded()) {
                searchMenu.collapseActionView();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.help) {
            startActivity(new Intent(getActivity(), HelpActivity.class));
            return true;
        } else if (i == R.id.profile) {
            ActorSDK.sharedActor().startSettingActivity(getActivity());
            return true;
        }

        return false;
    }

    public class HomePagerAdapter extends FragmentNoMenuStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case 0:
                    return getDialogsFragment(new DialogsFragment());
                case 1:
                    return getContactsFragment(new ContactsFragment());

            }
        }

        @NonNull
        public ContactsFragment getContactsFragment(ContactsFragment res2) {
            res2.setHasOptionsMenu(false);
            return res2;
        }

        @NonNull
        public DialogsFragment getDialogsFragment(DialogsFragment res1) {
            Bundle arguments = new Bundle();
            arguments.putString("invite_url", joinGroupUrl);
            res1.setArguments(arguments);
            res1.setHasOptionsMenu(false);
            return res1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                case 0:
                    return getActivity().getString(R.string.main_bar_chats);
                case 1:
                    return getActivity().getString(R.string.main_bar_contacts);
            }
        }

        @Override
        public int getPageIconResId(int position, Context context) {
            return -1;
        }
    }
}
