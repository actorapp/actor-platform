package im.actor.messenger.storage;

import com.droidkit.engine.search.SearchEngine;
import com.droidkit.engine.search.UiSearch;
import com.droidkit.engine.search.sqlite.SqLiteAdapter;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.storage.adapters.SearchBserAdapter;

import im.actor.messenger.storage.scheme.GlobalSearch;

/**
 * Created by ex3ndr on 19.09.14.
 */
public final class SearchEngines {

    private static final SearchEngine<GlobalSearch> USERS_SEARCH =
            new SearchEngine<GlobalSearch>(
                    new SearchBserAdapter<GlobalSearch>(GlobalSearch.class),
                    new SqLiteAdapter(DbProvider.getDatabase(AppContext.getContext()), "USERS_SEARCH"));

    private static final UiSearch<GlobalSearch> USERS_SEARCH_UI = new UiSearch<GlobalSearch>(USERS_SEARCH);

    public static UiSearch<GlobalSearch> userSearch() {
        return USERS_SEARCH_UI;
    }

    public static SearchEngine<GlobalSearch> userSearchEngine() {
        return USERS_SEARCH;
    }

    private SearchEngines() {
    }
}
