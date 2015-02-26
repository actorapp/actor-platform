package im.actor.messenger.app.fragment.chat.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.droidkit.engine.list.view.EngineUiList;
import com.droidkit.engine.list.view.ListState;
import com.droidkit.engine.uilist.UiList;
import com.droidkit.engine.uilist.UiListListener;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.AbsContent;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.ServiceContent;
import im.actor.model.entity.content.VideoContent;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class RecyclerMessagesAdapter extends RecyclerView.Adapter<BaseHolder> {

    private static final int LOAD_GAP = 20;

    private EngineUiList<Message> engine;
    private UiList<Message> uiList;
    private MessagesFragment messagesFragment;
    private Context context;
    private UiListListener listListener;

    public RecyclerMessagesAdapter(MessagesFragment messagesFragment) {
        setHasStableIds(true);

        this.messagesFragment = messagesFragment;
        this.context = messagesFragment.getActivity();
        this.engine = messagesFragment.getListEngine();
        this.uiList = this.engine.getUiList();

        this.listListener = new UiListListener() {
            @Override
            public void onListUpdated() {
                notifyDataSetChanged();
            }
        };

        this.uiList.addListener(listListener);
    }

    @Override
    public int getItemCount() {
        return uiList.getSize() + 1;
    }

    public Message getItem(int position) {
        return uiList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return 0;
        } else {
            return getItem(position - 1).getListId();
        }
    }

    @Override
    public int getItemViewType(int position) {
        //Header
        if (position == 0) {
            return 0;
        } else {
            AbsContent content = getItem(position - 1).getContent();
            if (content instanceof ServiceContent) {
                return 1;
            } else if (content instanceof PhotoContent) {
                return 2;
            } else if (content instanceof VideoContent) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    @Override
    public BaseHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                return new FooterHolder(messagesFragment.getActivity());
            case 1:
                return new ServiceHolder(messagesFragment,
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.adapter_dialog_service, viewGroup, false));
            case 2:
                return new PhotoHolder(messagesFragment,
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.adapter_dialog_photo, viewGroup, false));
            case 3:
                return new TextHolder(messagesFragment,
                        LayoutInflater
                                .from(context)
                                .inflate(R.layout.adapter_dialog_text, viewGroup, false));
        }

        throw new RuntimeException("Unknown view type");
    }

    @Override
    public void onBindViewHolder(BaseHolder baseHolder, int i) {
        if (baseHolder instanceof MessageHolder) {
            onBindViewHolder((MessageHolder) baseHolder, i - 1);
        }
    }

    public void onBindViewHolder(MessageHolder bubbleHolder, int position) {
        Message prev = null;
        Message next = null;
        if (position > 1) {
            next = getItem(position - 1);
        }
        if (position < getItemCount() - 2) {
            prev = getItem(position + 1);
        }
        bubbleHolder.bindData(getItem(position), prev, next);

        // Autoload if required
        if (position > getItemCount() - LOAD_GAP) {
            if (engine.getListState().getValue().getState() == ListState.State.LOADED) {
                // TODO: Load history
            } else {
                engine.requestLoadTail();
            }
        }
    }

    @Override
    public void onViewRecycled(BaseHolder holder) {
        if (holder instanceof MessageHolder) {
            ((MessageHolder) holder).unbind();
        }
    }

    public void dispose() {
        this.uiList.removeListener(listListener);
    }
}