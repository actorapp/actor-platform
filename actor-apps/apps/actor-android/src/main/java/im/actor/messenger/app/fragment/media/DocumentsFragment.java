package im.actor.messenger.app.fragment.media;

import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.ListView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.BaseFragment;
import im.actor.messenger.app.Intents;
import im.actor.model.entity.Peer;

/**
 * Created by ex3ndr on 18.10.14.
 */
public class DocumentsFragment extends BaseFragment {

    public static DocumentsFragment open(Peer peer) {
        Bundle args = new Bundle();
        args.putLong(Intents.EXTRA_CHAT_PEER, peer.getUnuqueId());
        DocumentsFragment res = new DocumentsFragment();
        res.setArguments(args);
        return res;
    }

    private int chatType;
    private int chatId;
    // private EngineUiList<Document> documentList;
    private ListView listView;
    // private DocumentAdapter adapter;
    private View emptyView;
    private boolean isInit = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatType = getArguments().getInt("CHAT_TYPE");
        chatId = getArguments().getInt("CHAT_ID");


//        ListEngine<Document> engine = ListEngines.getDocuments(DialogUids.getDialogUid(chatType, chatId));
//        documentList = new EngineUiList<Document>(engine);

        View res = inflater.inflate(R.layout.fragment_doc, container, false);
        emptyView = res.findViewById(R.id.noDocs);
        emptyView.setVisibility(View.GONE);
        listView = (ListView) res.findViewById(R.id.docsList);
        // adapter = new DocumentAdapter(documentList, getActivity());

        View footer = inflater.inflate(R.layout.adapter_doc_footer, listView, false);
        listView.addFooterView(footer, null, false);
        // listView.setAdapter(adapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                Document doc = (Document) parent.getItemAtPosition(position);
//
//                Downloaded d = downloaded().get(doc.getFileLocation().getFileId());
//
//                if (d != null) {
//                    // String fileName = d.fileName;
//
//                    getActivity().startActivity(Intents.openDoc(d));
//                }
//            }
//        });

        isInit = true;
//        getBinder().bind(documentList.getListState(), new Listener<ListState>() {
//            @Override
//            public void onUpdated(ListState listState) {
//                switch (listState.getAuthState()) {
//                    case LOADING_EMPTY:
//                        break;
//                    case LOADED_EMPTY:
//                        showView(emptyView, !isInit, false);
//                        goneView(listView, !isInit, false);
//                        break;
//                    case LOADED:
//                    default:
//                        goneView(emptyView, !isInit, false);
//                        showView(listView, !isInit, false);
//                        break;
//                }
//            }
//        });
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
                // documentList.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (documentList != null) {
//            documentList.release();
//            documentList = null;
//        }
//        if (adapter != null) {
//            adapter.dispose();
//            adapter = null;
//        }
        listView = null;
        emptyView = null;
    }
}
