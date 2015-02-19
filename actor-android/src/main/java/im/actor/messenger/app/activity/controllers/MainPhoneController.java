package im.actor.messenger.app.activity.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiListListener;
import com.droidkit.mvvm.ValueChangeListener;
import com.droidkit.mvvm.ui.Listener;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.AddContactActivity;
import im.actor.messenger.app.activity.ComposeActivity;
import im.actor.messenger.app.activity.CreateGroupActivity;
import im.actor.messenger.app.activity.HelpActivity;
import im.actor.messenger.app.activity.MainActivity;
import im.actor.messenger.app.activity.MyProfileActivity;
import im.actor.messenger.app.fragment.contacts.ContactsFragment;
import im.actor.messenger.app.fragment.dialogs.DialogsFragment;
import im.actor.messenger.app.fragment.search.SearchAdapter;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.app.view.AvatarDrawable;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.PagerSlidingTabStrip;
import im.actor.messenger.model.ProfileSyncState;
import im.actor.messenger.util.Screen;
import im.actor.model.entity.Dialog;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.core.Core.messenger;
import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.users;

/**
 * Created by ex3ndr on 25.10.14.
 */
public class MainPhoneController extends MainBaseController implements ValueChangeListener<ProfileSyncState.State> {

    private ViewPager pager;

    private HomePagerAdapter homePagerAdapter;

    private ListView searchList;
    private View searchContainer;
    private View searchEmptyView;
    private View searchHintView;

    private SearchView searchView;
    private MenuItem searchMenu;
    private SearchAdapter searchAdapter;

    private PagerSlidingTabStrip barTabs;

    private View syncInProgressView;
    private View emptyContactsView;

    private View fabContent;
    private View fabRoot;

    private ImageView emptyContactsImage;
    private ImageView emptyDialogsImage;

    private boolean isFabVisible = false;

    public MainPhoneController(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onItemClicked(Dialog item) {
        startActivity(Intents.openDialog(item.getPeer(), false, getActivity()));
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        setContentView(R.layout.activity_main);

        getActivity().setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));

        syncInProgressView = findViewById(R.id.syncInProgress);
        emptyContactsView = findViewById(R.id.emptyContacts);
        emptyContactsImage = (ImageView) findViewById(R.id.emptyContactsImage);
        emptyDialogsImage = (ImageView) findViewById(R.id.emptyDialogsImage);

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

        searchList = (ListView) findViewById(R.id.searchList);
        searchContainer = findViewById(R.id.searchCont);
        searchEmptyView = findViewById(R.id.empty);
        searchHintView = findViewById(R.id.searchHint);
//        searchAdapter = new SearchAdapter(SearchEngines.userSearch().getResultList(), getActivity());
//        searchList.setAdapter(searchAdapter);
//        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object pos = parent.getItemAtPosition(position);
//                if (pos != null && pos instanceof GlobalSearch) {
//                    startActivity(Intents.openDialog(((GlobalSearch) pos).getContType(), ((GlobalSearch) pos).getContId(), false,
//                            getActivity()));
//                    searchMenu.collapseActionView();
//                }
//            }
//        });
//        SearchEngines.userSearch().getResultList().addListener(new UiListListener() {
//            @Override
//            public void onListUpdated() {
//                if (SearchEngines.userSearch().getResultList().getSize() > 0) {
//                    goneView(searchHintView);
//                    goneView(searchEmptyView);
//                    showView(searchList);
//                } else {
//                    if (SearchEngines.userSearch().getCurrentQuery().length() > 0) {
//                        goneView(searchHintView);
//                        showView(searchEmptyView);
//                    } else {
//                        showView(searchHintView);
//                        goneView(searchEmptyView);
//                    }
//                    goneView(searchList);
//                }
//            }
//        });

        pager = (ViewPager) findViewById(R.id.vp_pager);
        homePagerAdapter = new HomePagerAdapter(getFragmentManager());
        pager.setAdapter(homePagerAdapter);

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
        barTabs.setIndicatorColorResource(R.color.main_tab_selected);
        barTabs.setIndicatorHeight(Screen.dp(4));
        barTabs.setDividerColorResource(R.color.main_tab_divider);
        barTabs.setUnderlineHeight(0);

        barTabs.setViewPager(pager);
        tabsContainer.addView(barTabs, new FrameLayout.LayoutParams(Screen.dp(72 * 2), Screen.dp(56)));
        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(Screen.dp(72 * 2), Screen.dp(56));
        tabsContainer.setLayoutParams(lp);
        ab.setCustomView(tabsContainer);

        ProfileSyncState.getSyncState().addUiSubscriber(this);

        emptyContactsImage.setImageResource(R.drawable.contacts_empty_large);
        emptyDialogsImage.setImageResource(R.drawable.contacts_empty_large);
    }

    public void onChanged(ProfileSyncState.State value) {
        barTabs.setVisibility(View.VISIBLE);
        emptyContactsView.setVisibility(View.GONE);
        syncInProgressView.setVisibility(View.GONE);
//        switch (value) {
//            default:
//            case READY:
//                barTabs.setVisibility(View.VISIBLE);
//                emptyContactsView.setVisibility(View.GONE);
//                syncInProgressView.setVisibility(View.GONE);
//                getActivity().invalidateOptionsMenu();
//                break;
//            case EMPTY_APP:
//                barTabs.setVisibility(View.GONE);
//                emptyContactsView.setVisibility(View.VISIBLE);
//                syncInProgressView.setVisibility(View.GONE);
//                getActivity().invalidateOptionsMenu();
//                break;
//            case IN_PROGRESS:
//                barTabs.setVisibility(View.GONE);
//                emptyContactsView.setVisibility(View.GONE);
//                syncInProgressView.setVisibility(View.VISIBLE);
//                getActivity().invalidateOptionsMenu();
//                break;
//        }
    }

    @Override
    public void onPause() {
        ProfileSyncState.getSyncState().removeUiSubscriber(this);
        emptyContactsImage.setImageBitmap(null);
        emptyDialogsImage.setImageBitmap(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem menuItem = menu.findItem(R.id.profile);
        final AvatarView avatarView = (AvatarView) menuItem.getActionView().findViewById(R.id.avatarView);
        if (messenger().isLoggedIn()) {
            UserVM userModel = users().get(myUid());
            if (userModel != null) {
                avatarView.setEmptyDrawable(AvatarDrawable.create(userModel, 18, getActivity()));
                getActivity().bind(avatarView, userModel.getAvatar());
            }
        }
        menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MyProfileActivity.class));
            }
        });

        searchMenu = menu.findItem(R.id.search);
        searchMenu.setVisible(ProfileSyncState.getSyncState().getValue() == ProfileSyncState.State.READY);
        searchView = (SearchView) searchMenu.getActionView();
        searchView.setIconifiedByDefault(true);

//        SearchViewHacker.setIcon(searchView, R.drawable.bar_search);
//        SearchViewHacker.setCloseIcon(searchView, R.drawable.bar_clear_search);
//        SearchViewHacker.setHint(searchView, "", R.drawable.bar_search,
//                getResources().getColor(R.color.text_hint_light),
//                getResources());
//        SearchViewHacker.setEditText(searchView, R.drawable.search_selector);
//        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                SearchEngines.userSearch().clear();
//                showView(searchContainer);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                SearchEngines.userSearch().clear();
//                goneView(searchContainer);
//                return true;
//            }
//        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                searchAdapter.setQuery(newText.toLowerCase());
//                SearchEngines.userSearch().query(newText);
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onBackPressed() {
        if (isFabVisible) {
            goneFab();
            return true;
        }
        if (searchMenu != null) {
            if (searchMenu.isActionViewExpanded()) {
                searchMenu.collapseActionView();
                return true;
            }
        }
        return false;
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

    public class HomePagerAdapter extends FragmentStatePagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

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
                    return new DialogsFragment();
                case 1:
                    return new ContactsFragment();
            }
        }

        @Override
        public int getPageIconResId(int position) {
            switch (position) {
                default:
                case 0:
                    return R.drawable.main_bar_recent_selector;
                case 1:
                    return R.drawable.main_bar_contacts_selector;
            }
        }
    }
}
