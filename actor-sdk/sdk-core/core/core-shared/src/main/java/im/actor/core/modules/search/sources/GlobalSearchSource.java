package im.actor.core.modules.search.sources;

import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.Group;
import im.actor.core.entity.PeerSearchEntity;
import im.actor.core.entity.PeerType;
import im.actor.core.entity.SearchEntity;
import im.actor.core.entity.SearchResult;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.function.Consumer;
import im.actor.runtime.mvvm.SearchValueSource;
import im.actor.runtime.storage.ListEngine;
import im.actor.runtime.storage.ListEngineDisplayExt;

public class GlobalSearchSource extends AbsModule implements SearchValueSource<SearchResult> {

    public GlobalSearchSource(ModuleContext context) {
        super(context);
    }

    @Override
    public void loadResults(String query, Consumer<List<SearchResult>> callback) {
        ListEngine<SearchEntity> searchList = context().getSearchModule().getSearchList();
        if (searchList instanceof ListEngineDisplayExt) {
            ((ListEngineDisplayExt<SearchEntity>) searchList).loadBackward(query, 20, (items, topSortKey, bottomSortKey) -> {
                ArrayList<SearchResult> localResults = new ArrayList<>();
                for (SearchEntity e : items) {
                    localResults.add(new SearchResult(e.getPeer(), e.getAvatar(), e.getTitle(),
                            null));
                }
                callback.apply(new ArrayList<>(localResults));
                if (query.length() > 3) {
                    loadGlobalResults(query, localResults, callback);
                }
            });
        } else {
            if (query.length() > 3) {
                loadGlobalResults(query, new ArrayList<>(), callback);
            } else {
                callback.apply(new ArrayList<>());
            }
        }
    }


    private void loadGlobalResults(String query, ArrayList<SearchResult> localResults, Consumer<List<SearchResult>> callback) {
        context().getSearchModule().findPeers(query).then(r -> {
            ArrayList<SearchResult> results = new ArrayList<>();
            outer:
            for (PeerSearchEntity peerSearch : r) {
                for (SearchResult l : localResults) {
                    if (peerSearch.getPeer().equals(l.getPeer())) {
                        continue outer;
                    }
                }
                if (peerSearch.getPeer().getPeerType() == PeerType.GROUP) {
                    Group group = context().getGroupsModule().getGroups().getValue(peerSearch.getPeer().getPeerId());
                    results.add(new SearchResult(peerSearch.getPeer(), group.getAvatar(), group.getTitle(),
                            peerSearch.getOptMatchString()));
                } else if (peerSearch.getPeer().getPeerType() == PeerType.PRIVATE) {
                    UserVM user = context().getUsersModule().getUsers().get(peerSearch.getPeer().getPeerId());
                    results.add(new SearchResult(peerSearch.getPeer(), user.getAvatar().get(), user.getName().get(),
                            peerSearch.getOptMatchString()));
                }
            }
            if (results.size() > 0) {
                ArrayList<SearchResult> combined = new ArrayList<>();
                combined.addAll(localResults);
                combined.addAll(results);
                callback.apply(combined);
            }
        });
    }
}
