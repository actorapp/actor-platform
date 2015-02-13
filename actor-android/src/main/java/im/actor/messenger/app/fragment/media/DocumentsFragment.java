package im.actor.messenger.app.fragment.media;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import com.droidkit.engine.list.ListEngine;
import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.list.view.ListState;
import com.droidkit.mvvm.ui.Listener;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseCompatFragment;
import im.actor.messenger.app.fragment.media.adapters.DocumentAdapter;
import im.actor.messenger.app.intents.Intents;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.media.Document;
import im.actor.messenger.storage.scheme.media.Downloaded;

import static im.actor.messenger.storage.KeyValueEngines.downloaded;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DocumentsFragment extends BaseCompatFragment {

    public static DocumentsFragment open(int chatType, int chatId) {
        Bundle args = new Bundle();
        args.putInt("CHAT_TYPE", chatType);
        args.putInt("CHAT_ID", chatId);
        DocumentsFragment res = new DocumentsFragment();
        res.setArguments(args);
        return res;
    }

    private int chatType;
    private int chatId;
    private EngineUiList<Document> documentList;
    private ListView listView;
    private DocumentAdapter adapter;
    private View emptyView;
    private boolean isInit = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatType = getArguments().getInt("CHAT_TYPE");
        chatId = getArguments().getInt("CHAT_ID");


        ListEngine<Document> engine = ListEngines.getDocuments(DialogUids.getDialogUid(chatType, chatId));
        documentList = new EngineUiList<Document>(engine);

        View res = inflater.inflate(R.layout.fragment_doc, container, false);
        emptyView = res.findViewById(R.id.noDocs);
        emptyView.setVisibility(View.GONE);
        listView = (ListView) res.findViewById(R.id.docsList);
        adapter = new DocumentAdapter(documentList, getActivity());

        View footer = inflater.inflate(R.layout.adapter_doc_footer, listView, false);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Document doc = (Document) parent.getItemAtPosition(position);

                Downloaded d = downloaded().get(doc.getFileLocation().getFileId());

                if (d != null) {
                    // String fileName = d.fileName;

                    getActivity().startActivity(Intents.openDoc(d));
                }
            }
        });

        isInit = true;
        getBinder().bind(documentList.getListState(), new Listener<ListState>() {
            @Override
            public void onUpdated(ListState listState) {
                switch (listState.getState()) {
                    case LOADING_EMPTY:
                        break;
                    case LOADED_EMPTY:
                        showView(emptyView, !isInit, false);
                        goneView(listView, !isInit, false);
                        break;
                    case LOADED:
                    default:
                        goneView(emptyView, !isInit, false);
                        showView(listView, !isInit, false);
                        break;
                }
            }
        });
        isInit = false;

        return res;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.docs, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                documentList.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (documentList != null) {
            documentList.release();
            documentList = null;
        }
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        listView = null;
        emptyView = null;
    }
}
