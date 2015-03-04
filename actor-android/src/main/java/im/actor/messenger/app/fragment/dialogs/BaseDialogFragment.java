package im.actor.messenger.app.fragment.dialogs;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiListStateListener;

import java.util.HashMap;
import java.util.HashSet;

import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.MaterialInterpolator;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.util.Screen;
import im.actor.model.entity.Dialog;

import static im.actor.messenger.app.Core.messenger;

/**
 * Created by ex3ndr on 22.11.14.
 */
public class BaseDialogFragment extends BaseFragment implements UiListStateListener {

    private ListView listView;

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

        adapter = new DialogsAdapter(ListEngines.getChatsUiListEngine(), getActivity(), new OnItemClickedListener<Dialog>() {
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

        listView = (ListView) res.findViewById(R.id.list);
        FrameLayout footer = new FrameLayout(getActivity());
        footer.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        footer.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(160)));
        ImageView shadow = new ImageView(getActivity());
        shadow.setImageResource(R.drawable.card_shadow_bottom);
        shadow.setScaleType(ImageView.ScaleType.FIT_XY);
        shadow.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(4)));
        footer.addView(shadow);
        TextView hint = new TextView(getActivity());

        hint.setText(R.string.dialogs_hint);
        hint.setTypeface(Fonts.regular());
        hint.setPadding(Screen.dp(16), Screen.dp(8), Screen.dp(16), 0);
        hint.setGravity(Gravity.CENTER);
        hint.setTextSize(16);
        hint.setTextColor(getResources().getColor(R.color.text_subheader));
        hint.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        footer.addView(hint);
        listView.addFooterView(footer, null, false);

        TextView header = new TextView(getActivity());
        header.setBackgroundColor(getResources().getColor(R.color.bg_grey));
        header.setText(R.string.dialogs_title);
        header.setTypeface(Fonts.bold());
        header.setPadding(Screen.dp(16), 0, 0, 0);
        header.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        header.setTextSize(16);
        header.setTextColor(getResources().getColor(R.color.text_subheader));

        LinearLayout headerCont = new LinearLayout(getActivity());
        headerCont.setBackgroundColor(getResources().getColor(R.color.bg_light));
        headerCont.setOrientation(LinearLayout.VERTICAL);
        headerCont.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(48)));
        headerCont.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        listView.addHeaderView(headerCont, null, false);

        listView.setAdapter(adapter);
        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                adapter.onMovedToScrapHeap(view);
            }
        });

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
        ListEngines.getChatsUiListEngine().getUiList().addExListener(this);
        adapter.resume();
        dialogsEmptyImage.setImageResource(R.drawable.dialogs_empty_large);
        messenger().onDialogsOpen();
    }

    @Override
    public void onPause() {
        super.onPause();
        ListEngines.getChatsUiListEngine().getUiList().removeExListener(this);
        adapter.pause();
        dialogsEmptyImage.setImageBitmap(null);
        messenger().onDialogsClosed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        listView = null;
        noMessages = null;
        adapter = null;
    }

    private HashMap<Long, Integer> offsets;
    private long[] ids;

    @Override
    public void onListPreUpdated() {
        offsets = new HashMap<Long, Integer>();
        ids = new long[listView.getChildCount()];

        for (int i = 0; i < listView.getChildCount(); i++) {
            long id = listView.getItemIdAtPosition(
                    listView.getFirstVisiblePosition() + i);
            ids[i] = id;
            View view = listView.getChildAt(i);
            offsets.put(id, view.getTop());
        }
    }

    @Override
    public void onListPostUpdated() {

        adapter.notifyDataSetChanged();

        boolean changed = false;

        long[] idsNew = new long[listView.getChildCount()];

        if (idsNew.length != ids.length) {
            changed = true;
            for (int i = 0; i < listView.getChildCount(); i++) {
                long id = listView.getItemIdAtPosition(
                        listView.getFirstVisiblePosition() + i);
                idsNew[i] = id;
            }
        } else {
            for (int i = 0; i < listView.getChildCount(); i++) {
                long id = listView.getItemIdAtPosition(
                        listView.getFirstVisiblePosition() + i);
                idsNew[i] = id;
                if (ids[i] != id) {
                    changed = true;
                }
            }
        }

        if (changed) {
            final HashSet<Long> changedIds = new HashSet<Long>();
            for (int i = 0; i < Math.min(ids.length, idsNew.length); i++) {
                if (ids[i] != idsNew[i]) {
                    changedIds.add(ids[i]);
                    changedIds.add(idsNew[i]);
                }
            }

            if (ids.length < idsNew.length) {
                for (int i = ids.length; i < idsNew.length; i++) {
                    changedIds.add(idsNew[i]);
                }
            }

            if (idsNew.length < ids.length) {
                for (int i = idsNew.length; i < ids.length; i++) {
                    changedIds.add(ids[i]);
                }
            }

            listView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (listView == null)
                        return true;

                    listView.getViewTreeObserver().removeOnPreDrawListener(this);

                    for (int i = 1; i < listView.getChildCount() - 1; i++) {
                        View view = listView.getChildAt(i);
                        long id = listView.getItemIdAtPosition(
                                listView.getFirstVisiblePosition() + i);
                        if (changedIds.contains(id) && offsets.containsKey(id)) {
                            int oldTop;
                            if (offsets.containsKey(id)) {
                                oldTop = offsets.get(id);
                            } else {
                                oldTop = -view.getHeight();
                            }
                            int newTop = view.getTop();
                            if (oldTop != newTop) {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", oldTop - newTop, 0)
                                        .setDuration(300);
                                animator.setInterpolator(new MaterialInterpolator());
                                animator.start();
                            }
                        }
                    }

                    offsets.clear();

                    return true;
                }
            });
        }
    }


}
