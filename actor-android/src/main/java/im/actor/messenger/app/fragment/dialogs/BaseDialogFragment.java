package im.actor.messenger.app.fragment.dialogs;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.entity.Dialog;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class BaseDialogFragment extends BaseFragment {

    private ImageView dialogsEmptyImage;

    private View loadingProgress;
    private View noMessages;

    private DialogsAdapter adapter;

    public BaseDialogFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_dialogs, container, false);

        loadingProgress = res.findViewById(R.id.loadingProgress);
        noMessages = res.findViewById(R.id.noMessages);
        dialogsEmptyImage = (ImageView) res.findViewById(R.id.emptyDialogsImage);

        adapter = new DialogsAdapter(messenger().getDialogsGlobalList(), getActivity(), new OnItemClickedListener<Dialog>() {
            @Override
            public void onClicked(Dialog item) {
                onItemClick(item);
            }
        }, supportLongClick() ? new OnItemClickedListener<Dialog>() {
            @Override
            public void onClicked(final Dialog dialog) {
                onItemLongClick(dialog);
            }
        } : null);

        RecyclerView messagesView = new RecyclerView(getActivity());
        messagesView.setVerticalScrollBarEnabled(true);
        messagesView.setHorizontalScrollBarEnabled(false);
        messagesView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(false);
        messagesView.setLayoutManager(linearLayoutManager);
        messagesView.setAdapter(adapter);

        ((LinearLayout) res.findViewById(R.id.collectionContainer)).addView(
                messagesView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        );

//        FrameLayout footer = new FrameLayout(getActivity());
//        footer.setBackgroundColor(getResources().getColor(R.color.bg_grey));
//        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(160)));
//        ImageView shadow = new ImageView(getActivity());
//        shadow.setImageResource(R.drawable.card_shadow_bottom);
//        shadow.setScaleType(ImageView.ScaleType.FIT_XY);
//        shadow.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
//        footer.addView(shadow);
//        TextView hint = new TextView(getActivity());
//
//        hint.setText(R.string.dialogs_hint);
//        hint.setTypeface(Fonts.regular());
//        hint.setPadding(Screen.dp(16), Screen.dp(8), Screen.dp(16), 0);
//        hint.setGravity(Gravity.CENTER);
//        hint.setTextSize(16);
//        hint.setTextColor(getResources().getColor(R.color.text_subheader));
//        hint.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        footer.addView(hint);
//        listView.addFooterView(footer, null, false);
//
//        TextView header = new TextView(getActivity());
//        header.setBackgroundColor(getResources().getColor(R.color.bg_grey));
//        header.setText(R.string.dialogs_title);
//        header.setTypeface(Fonts.bold());
//        header.setPadding(Screen.dp(16), 0, 0, 0);
//        header.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//        header.setTextSize(16);
//        header.setTextColor(getResources().getColor(R.color.text_subheader));
//
//        LinearLayout headerCont = new LinearLayout(getActivity());
//        headerCont.setBackgroundColor(getResources().getColor(R.color.bg_light));
//        headerCont.setOrientation(LinearLayout.VERTICAL);
//        headerCont.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));
//        headerCont.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        listView.addHeaderView(headerCont, null, false);

//        listView.setAdapter(adapter);
//        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
//            @Override
//            public void onMovedToScrapHeap(View view) {
//                adapter.onMovedToScrapHeap(view);
//            }
//        });

        loadingProgress.setVisibility(View.GONE);
        noMessages.setVisibility(View.GONE);
//        getBinder().bind(ListEngines.getChatsUiListEngine().getListState(), new Listener<ListState>() {
//            @Override
//            public void onUpdated(ListState listState) {
//                switch (listState.getState()) {
//                    case LOADED:
//                        loadingProgress.setVisibility(View.GONE);
//                        noMessages.setVisibility(View.GONE);
//                        break;
//                    case LOADED_EMPTY:
//                        loadingProgress.setVisibility(View.GONE);
//                        noMessages.setVisibility(View.VISIBLE);
//                        break;
//                    case LOADING_EMPTY:
//                        loadingProgress.setVisibility(View.VISIBLE);
//                        noMessages.setVisibility(View.GONE);
//                        break;
//                }
//            }
//        });

        return res;
    }

    protected boolean supportLongClick() {
        return false;
    }

    protected void onItemClick(Dialog item) {

    }

    protected void onItemLongClick(final Dialog dialog) {

    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.resume();
        messenger().onDialogsOpen();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.pause();
        messenger().onDialogsClosed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.pause();
            adapter = null;
        }
        noMessages = null;
        adapter = null;
    }
}
