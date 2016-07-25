package im.actor.runtime.mvvm;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.annotations.MainThread;

public class SearchValueModel<T> extends AsyncVM {

    private SearchValueSource<T> searchValueSource;
    private int requestId = 0;
    private ValueModel<List<T>> results;

    public SearchValueModel(SearchValueSource<T> searchValueSource) {
        this.searchValueSource = searchValueSource;
        this.results = new ValueModel<>("search.results", new ArrayList<>());
    }

    public ValueModel<List<T>> getResults() {
        return results;
    }

    @MainThread
    public void queryChanged(String query) {

        final int currentRequestId = ++requestId;

        // Filtering out trivial sources
        if (query == null) {
            postResults(new ArrayList<>(), currentRequestId);
            return;
        }
        query = query.trim();
        if (query.length() == 0) {
            postResults(new ArrayList<>(), currentRequestId);
            return;
        }

        // Non-trivial
        searchValueSource.loadResults(query, r -> {
            if (currentRequestId == requestId) {
                postResults(r, currentRequestId);
            }
        });
    }

    @MainThread
    protected void onResultsReceived(List<T> res) {
        results.changeInUIThread(res);
    }

    //
    // Internal Loop
    //

    private void postResults(List<T> res, int requestIndex) {
        post(new Results<>(res, requestIndex));
    }

    @Override
    protected void onObjectReceived(Object obj) {
        if (obj instanceof Results) {
            Results<T> r = (Results<T>) obj;
            if (r.getRequestIndex() == requestId) {
                onResultsReceived(r.getRes());
            }
        }
    }

    protected static class Results<T> {

        private List<T> res;
        private int requestIndex;

        public Results(List<T> res, int requestIndex) {
            this.res = res;
            this.requestIndex = requestIndex;
        }

        public List<T> getRes() {
            return res;
        }

        public int getRequestIndex() {
            return requestIndex;
        }
    }
}