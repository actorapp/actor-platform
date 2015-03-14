package im.actor.messenger.app.fragment.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.HelpActivity;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.model.entity.Contact;
import im.actor.model.mvvm.BindedDisplayList;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 23.09.14.
 */
public abstract class BaseContactFragment extends BaseFragment {

    private ContactsAdapter adapter;
    private BindedDisplayList<Contact> contactDisplayList;

    private View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_base_contacts, container, false);
        emptyView = res.findViewById(R.id.notingFound);
        emptyView.setVisibility(View.GONE);

        RecyclerView recyclerView = (RecyclerView) res.findViewById(R.id.collection);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        contactDisplayList = messenger().getContactsGlobalList();
        adapter = new ContactsAdapter(contactDisplayList, getActivity(), false);

        recyclerView.setAdapter(adapter);

//        View header = new View(getActivity());
//        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
//        listView.addHeaderView(header, null, false);

//        engineUiList = new EngineUiList<Contact>(ListEngines.getContactsListEngine());
//        adapter = new ContactsAdapter(engineUiList, getActivity(), false, new OnItemClickedListener<Contact>() {
//            @Override
//            public void onClicked(Contact item) {
//                onUserSelected((int) (long) item.getUid());
//                getActivity().finish();
//            }
//        }, null);
//        listView.setAdapter(adapter);

//        getBinder().bind(engineUiList.getListState(), new Listener<ListState>() {
//            @Override
//            public void onUpdated(ListState listState) {
//                switch (listState.getAuthState()) {
//                    case LOADING_EMPTY:
//                        break;
//                    case LOADED_EMPTY:
//                        showView(emptyView, true, false);
//                        goneView(listView, true, false);
//                        break;
//                    case LOADED:
//                    default:
//                        goneView(emptyView, true, false);
//                        showView(listView, true, false);
//                        break;
//                }
//            }
//        });

        return res;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.resume();
        }
    }

    protected abstract void onUserSelected(int uid);

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.compose, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().length() == 0) {
                    contactDisplayList.initTop(false);
                } else {
                    contactDisplayList.initSearch(newText.trim(), false);
                }
                adapter.setQuery(newText.toLowerCase());
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.help) {
            startActivity(new Intent(getActivity(), HelpActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.pause();
            adapter = null;
        }
//        if (engineUiList != null) {
//            engineUiList.release();
//            engineUiList = null;
//        }
//        listView = null;
        emptyView = null;
    }
}
