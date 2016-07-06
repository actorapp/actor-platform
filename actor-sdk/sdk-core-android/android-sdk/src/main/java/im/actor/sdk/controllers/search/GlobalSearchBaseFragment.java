package im.actor.sdk.controllers.search;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ChatLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.core.entity.Peer;
import im.actor.core.entity.SearchEntity;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.generic.mvvm.DisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.controllers.fragment.main.SearchAdapter;
import im.actor.sdk.controllers.fragment.main.SearchHolder;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.HeaderViewRecyclerAdapter;
import im.actor.sdk.view.adapters.OnItemClickedListener;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public abstract class GlobalSearchBaseFragment extends BaseFragment {

    private MenuItem searchMenu;
    private SearchView searchView;

    private boolean isSearchVisible = false;
    private RecyclerView searchList;
    private View searchContainer;
    private TextView searchEmptyView;
    private TextView searchHintView;

    private SearchAdapter searchAdapter;
    private BindedDisplayList<SearchEntity> searchDisplay;
    private final DisplayList.Listener searchListener = () -> onSearchChanged();

    SearchHolder footerSearchHolder;
    private String searchQuery;
    private LinearLayout footer;

    public GlobalSearchBaseFragment() {
        setHasOptionsMenu(true);
        setUnbindOnPause(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_global_search, container, false);
        res.setVisibility(View.GONE);

        searchList = (RecyclerView) res.findViewById(R.id.searchList);
        searchList.setLayoutManager(new ChatLinearLayoutManager(getActivity()));
        searchList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && isSearchVisible) {
                    if (searchView != null) {
                        searchView.clearFocus();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });

        searchContainer = res.findViewById(R.id.searchCont);
        searchContainer.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        searchEmptyView = (TextView) res.findViewById(R.id.empty);
        searchHintView = (TextView) res.findViewById(R.id.searchHint);
        searchEmptyView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setTextColor(style.getTextSecondaryColor());
        searchHintView.setVisibility(View.GONE);
        searchEmptyView.setVisibility(View.GONE);

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();

        bind(messenger().getAppState().getIsAppLoaded(), messenger().getAppState().getIsAppEmpty(), (isAppLoaded, Value, isAppEmpty, Value2) -> {
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_global_search, menu);

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
                searchQuery = s.trim();

                if (isSearchVisible) {
                    if (s.trim().length() > 0) {
                        String activeSearchQuery = searchQuery;
                        searchDisplay.initSearch(s.trim().toLowerCase(), false);
                        searchAdapter.setQuery(s.trim().toLowerCase());
                        messenger().findUsers(s).start(new CommandCallback<UserVM[]>() {
                            @Override
                            public void onResult(UserVM[] res) {
                                int footerVisability = footer.getVisibility();
                                if (searchQuery.equals(activeSearchQuery)) {
                                    boolean showResult = false;
                                    UserVM u = null;
                                    if (res.length > 0) {
                                        u = res[0];
                                        showResult = true;
                                        for (int i = 0; i < searchDisplay.getSize(); i++) {
                                            if (searchDisplay.getItem(i).getPeer().equals(Peer.user(u.getId())))
                                                showResult = false;
                                            break;
                                        }
                                    }
                                    if (showResult) {
                                        footerSearchHolder.bind(new SearchEntity(Peer.user(u.getId()), 0, u.getAvatar().get(), u.getName().get()), activeSearchQuery, true);
                                        showView(footer);
                                    } else {
                                        goneView(footer);
                                    }
                                }
                                if (footerVisability != footer.getVisibility()) {
                                    onSearchChanged();
                                }
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    } else {
                        searchDisplay.initEmpty();
                        goneView(footer);

                    }
                }
                return false;
            }
        });
    }

    private void onSearchChanged() {
        if (!searchDisplay.isInSearchState()) {
            showView(searchHintView);
            goneView(searchEmptyView);
        } else {
            goneView(searchHintView);
            if (searchDisplay.getSize() == 0 && footer.getVisibility() != View.VISIBLE) {
                showView(searchEmptyView);
            } else {
                goneView(searchEmptyView);
            }
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
                onPeerPicked(item.getPeer());
                searchMenu.collapseActionView();
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

        TextView footerTitle = new TextView(getActivity());
        footerTitle.setText(R.string.main_search_global_header);
        footerTitle.setTextSize(16);
        footerTitle.setPadding(Screen.dp(12), Screen.dp(8), 0, Screen.dp(8));
        footerTitle.setBackgroundColor(ActorSDK.sharedActor().style.getBackyardBackgroundColor());
        footerTitle.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());

        footerSearchHolder = new SearchHolder(getActivity(), new OnItemClickedListener<SearchEntity>() {
            @Override
            public void onClicked(SearchEntity item) {
                int peerId = item.getPeer().getPeerId();
                execute(messenger().addContact(peerId), R.string.progress_common, new CommandCallback<Boolean>() {
                    @Override
                    public void onResult(Boolean res2) {
                        startActivity(Intents.openPrivateDialog(peerId,
                                true,
                                getActivity()));
                    }

                    @Override
                    public void onError(Exception e) {
                        startActivity(Intents.openPrivateDialog(peerId,
                                true,
                                getActivity()));
                    }
                });
            }

            @Override
            public boolean onLongClicked(SearchEntity item) {
                return false;
            }
        });
        View footerGlobalSearchView = footerSearchHolder.itemView;

        footer = new LinearLayout(getActivity());
        footer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Screen.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        footer.addView(footerTitle, params);
        footer.addView(footerGlobalSearchView, params);

        footer.setVisibility(View.GONE);

        recyclerAdapter.addFooterView(footer);

        searchList.setAdapter(recyclerAdapter);
        searchDisplay.addListener(searchListener);
        showView(searchHintView, false);
        goneView(searchEmptyView, false);

        showView(searchContainer, false);

        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof GlobalSearchStateDelegate) {
            ((GlobalSearchStateDelegate) parent).onGlobalSearchStarted();
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

        goneView(searchContainer, false);
        if (searchMenu != null) {
            if (searchMenu.isActionViewExpanded()) {
                searchMenu.collapseActionView();
            }
        }

        Fragment parent = getParentFragment();
        if (parent != null && parent instanceof GlobalSearchStateDelegate) {
            ((GlobalSearchStateDelegate) parent).onGlobalSearchEnded();
        }
    }

    protected abstract void onPeerPicked(Peer peer);
}
