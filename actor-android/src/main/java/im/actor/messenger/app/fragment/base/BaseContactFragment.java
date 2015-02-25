package im.actor.messenger.app.fragment.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;

import im.actor.messenger.R;
import im.actor.messenger.app.activity.HelpActivity;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.util.Screen;

/**
 * Created by ex3ndr on 23.09.14.
 */
public abstract class BaseContactFragment extends BaseFragment {

    // private ContactsAdapter adapter;
    private ListView listView;
    private View emptyView;
    // private EngineUiList<Contact> engineUiList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_base_contacts, container, false);
        emptyView = res.findViewById(R.id.notingFound);
        emptyView.setVisibility(View.GONE);
        listView = (ListView) res.findViewById(R.id.contactsList);
        View header = new View(getActivity());
        header.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
        listView.addHeaderView(header, null, false);
//        engineUiList = new EngineUiList<Contact>(ListEngines.getContactsEngine());
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
                // engineUiList.filter(newText);
                // adapter.setQuery(newText.toLowerCase());
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
    public void onDestroyView() {
        super.onDestroyView();
//        if (adapter != null) {
//            adapter.dispose();
//            adapter = null;
//        }
//        if (engineUiList != null) {
//            engineUiList.release();
//            engineUiList = null;
//        }
        listView = null;
        emptyView = null;
    }
}
