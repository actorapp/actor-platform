package im.actor.messenger.app.activity.controllers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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

import java.util.ArrayList;

import im.actor.core.entity.Dialog;
import im.actor.core.entity.SearchEntity;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.activity.AddContactActivity;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.fragment.compose.ComposeActivity;
import im.actor.messenger.app.fragment.compose.CreateGroupActivity;
import im.actor.messenger.app.fragment.contacts.ContactsFragment;
import im.actor.messenger.app.fragment.dialogs.DialogsFragment;
import im.actor.messenger.app.fragment.help.HelpActivity;
import im.actor.messenger.app.fragment.main.SearchAdapter;
import im.actor.messenger.app.fragment.settings.MyProfileActivity;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.FragmentNoMenuStatePagerAdapter;
import im.actor.messenger.app.view.HeaderViewRecyclerAdapter;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.app.view.PagerSlidingTabStrip;
import im.actor.runtime.mvvm.ValueDoubleChangedListener;
import im.actor.runtime.mvvm.Value;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class MainPhoneController extends MainBaseController {

    private ViewPager pager;

    private HomePagerAdapter homePagerAdapter;

    private RecyclerView searchList;
    private View searchContainer;
    private View searchEmptyView;
    private View searchHintView;

    private boolean isSearchVisible = false;
    private final DisplayList.Listener searchListener = new DisplayList.Listener() {
        @Override
        public void onCollectionChanged() {
            onSearchChanged();
        }
    };
    private SearchAdapter searchAdapter;
    private BindedDisplayList<SearchEntity> searchDisplay;

    private SearchView searchView;
    private MenuItem searchMenu;

    private PagerSlidingTabStrip barTabs;

    private View syncInProgressView;
    private View emptyContactsView;

    private View fabContent;
    private View fabRoot;

    private boolean isFabVisible = false;

    private String joinGroupUrl;
    private String sendUriString = "";
    private String sendText = "";
    private ArrayList<String> sendUriMultiple = new ArrayList<String>();
    private int shareUser;
    private String forwardText = "";
    private String forwardTextRaw = "";
    private String forwardDocDescriptor = "";
    private boolean forwardDocIsDoc = true;

    public MainPhoneController(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onItemClicked(Dialog item) {
        startActivity(Intents.openDialog(item.getPeer(), false, getActivity()).putExtra("send_uri", sendUriString)
                .putExtra("send_uri_multiple", sendUriMultiple)
                .putExtra("send_text", sendText)
                .putExtra("forward_text", forwardText)
                .putExtra("forward_text_raw", forwardTextRaw)
                .putExtra("forward_doc_descriptor", forwardDocDescriptor)
                .putExtra("forward_doc_is_doc", forwardDocIsDoc)
                .putExtra("share_user", shareUser));
        sendUriMultiple.clear();
        sendUriString = "";
        forwardDocDescriptor = "";
        forwardText = "";
        forwardTextRaw = "";
        sendText = "";
        shareUser = 0;
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_VIEW) && intent.getData() != null) {
                    joinGroupUrl = getIntent().getData().toString();
                } else if (intent.getAction().equals(Intent.ACTION_SEND)) {
                    if ("text/plain".equals(getIntent().getType())) {
                        sendText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    } else {
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
                Bundle extras = getIntent().getExtras();
                if (extras.containsKey("share_user")) {
                    shareUser = extras.getInt("share_user");
                } else if (extras.containsKey("forward_text")) {
                    forwardText = extras.getString("forward_text");
                    forwardTextRaw = extras.getString("forward_text_raw");
                } else if (extras.containsKey("forward_doc_descriptor")) {
                    forwardDocDescriptor = extras.getString("forward_doc_descriptor");
                    forwardDocIsDoc = extras.getBoolean("forward_doc_is_doc");
                }
            }
        }

        setContentView(R.layout.activity_main);

        getActivity().setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));

        syncInProgressView = findViewById(R.id.syncInProgress);
        emptyContactsView = findViewById(R.id.emptyContacts);

        ((TextView) findViewById(R.id.addContactButtonText)).setTypeface(Fonts.medium());
        ((TextView) findViewById(R.id.inviteButtonText)).setTypeface(Fonts.medium());

        isFabVisible = false;

        fabContent = findViewById(R.id.fabContainer);
        fabRoot = findViewById(R.id.rootFab);

        fabRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFab();
            }
        });

        fabContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneFab();
            }
        });

        searchList = (RecyclerView) findViewById(R.id.searchList);
        searchList.setLayoutManager(new ChatLinearLayoutManager(getActivity()));

        searchContainer = findViewById(R.id.searchCont);
        searchEmptyView = findViewById(R.id.empty);
        searchHintView = findViewById(R.id.searchHint);
        searchHintView.setVisibility(View.GONE);
        searchEmptyView.setVisibility(View.GONE);

        pager = (ViewPager) findViewById(R.id.vp_pager);
        pager.setOffscreenPageLimit(2);
        homePagerAdapter = new HomePagerAdapter(getFragmentManager());
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

        findViewById(R.id.composeContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneFab();
                startActivity(new Intent(getActivity(), ComposeActivity.class));
            }
        });

        findViewById(R.id.createGroupContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneFab();
                startActivity(new Intent(getActivity(), CreateGroupActivity.class));
            }
        });

        findViewById(R.id.addContactContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneFab();
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });

        findViewById(R.id.addContactButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddContactActivity.class));
            }
        });

        findViewById(R.id.inviteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inviteMessage = getResources().getString(R.string.invite_message);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, inviteMessage);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    @Override
    public void onResume() {
        ActionBar ab = getActionBar();
        ab.setDisplayShowCustomEnabled(true);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayShowTitleEnabled(false);

        FrameLayout tabsContainer = new FrameLayout(getActivity());
        barTabs = new PagerSlidingTabStrip(getActivity());
        barTabs.setTabBackground(R.drawable.selector_bar);
        //barTabs.setIndicatorColorResource(R.color.main_tab_selected);
        barTabs.setIndicatorHeight(Screen.dp(2));

        barTabs.setDividerColorResource(R.color.primary);
        //barTabs.setTextColorResource(R.color.main_tab_text);
        barTabs.setTextSize(Screen.dp(14));
        barTabs.setUnderlineHeight(0);

        barTabs.setViewPager(pager);

        // Icons
        // int width = Screen.dp(72 * 2);
        int width = Screen.dp(120 * 2 + 72);

        tabsContainer.addView(barTabs, new FrameLayout.LayoutParams(width, Screen.dp(56)));
        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(width, Screen.dp(56));
        tabsContainer.setLayoutParams(lp);
        ab.setCustomView(tabsContainer);

        barTabs.setVisibility(View.VISIBLE);
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
                                barTabs.setVisibility(View.GONE);
                                emptyContactsView.setVisibility(View.VISIBLE);
                                syncInProgressView.setVisibility(View.GONE);
                                getActivity().invalidateOptionsMenu();
                            } else {
                                barTabs.setVisibility(View.GONE);
                                emptyContactsView.setVisibility(View.GONE);
                                syncInProgressView.setVisibility(View.VISIBLE);
                                getActivity().invalidateOptionsMenu();
                            }
                        } else {
                            barTabs.setVisibility(View.VISIBLE);
                            emptyContactsView.setVisibility(View.GONE);
                            syncInProgressView.setVisibility(View.GONE);
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });
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
        if (isFabVisible) {
            goneFab();
            return true;
        }
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

    private void showFab() {
        if (!isFabVisible) {
            isFabVisible = true;
            showView(fabContent, true, false);
        }
    }

    private void goneFab() {
        if (isFabVisible) {
            isFabVisible = false;
            goneView(fabContent, true, false);
        }
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
        header.setBackgroundColor(getActivity().getResources().getColor(R.color.bg_main));
        recyclerAdapter.addHeaderView(header);

        searchList.setAdapter(recyclerAdapter);
        searchDisplay.addListener(searchListener);

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

        if (searchMenu != null) {
            if (searchMenu.isActionViewExpanded()) {
                searchMenu.collapseActionView();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                startActivity(new Intent(getActivity(), HelpActivity.class));
                return true;
            case R.id.profile:
                startActivity(new Intent(getActivity(), MyProfileActivity.class));
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
                    DialogsFragment res1 = new DialogsFragment();
                    Bundle arguments = new Bundle();
                    arguments.putString("invite_url", joinGroupUrl);
                    res1.setArguments(arguments);
                    res1.setHasOptionsMenu(false);
                    return res1;
                case 1:
                    ContactsFragment res2 = new ContactsFragment();
                    res2.setHasOptionsMenu(false);
                    return res2;

            }
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
