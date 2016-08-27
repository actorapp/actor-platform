package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.view.ViewGroup;

import java.util.HashMap;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.sdk.controllers.conversation.messages.content.AudioHolder;
import im.actor.sdk.controllers.conversation.messages.content.ContactHolder;
import im.actor.sdk.controllers.conversation.messages.content.DocHolder;
import im.actor.sdk.controllers.conversation.messages.content.LocationHolder;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.messages.content.PhotoHolder;
import im.actor.sdk.controllers.conversation.messages.content.preprocessor.PreprocessedList;
import im.actor.sdk.controllers.conversation.messages.content.ServiceHolder;
import im.actor.sdk.controllers.conversation.messages.content.StickerHolder;
import im.actor.sdk.controllers.conversation.messages.content.TextHolder;
import im.actor.sdk.controllers.ActorBinder;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MessagesAdapter extends BindedListAdapter<Message, AbsMessageViewHolder> {

    private MessagesFragment messagesFragment;
    private ActorBinder BINDER = new ActorBinder();

    private Context context;
    private long firstUnread = -DefaultLayouter.SERVICE_HOLDER;
    private long readDate;
    private long receiveDate;
    private Peer peer;
    private ViewHolderMatcher matcher;

    private HashMap<Long, Message> selected = new HashMap<>();



    public MessagesAdapter(final BindedDisplayList<Message> displayList,
                           MessagesFragment messagesFragment, Context context) {
        super(displayList);

        matcher = new ViewHolderMatcher();

        matcher.add(new DefaultLayouter(DefaultLayouter.TEXT_HOLDER, TextHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.SERVICE_HOLDER, ServiceHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.PHOTO_HOLDER, PhotoHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.VOICE_HOLDER, AudioHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.DOCUMENT_HOLDER, DocHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.CONTACT_HOLDER, ContactHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.LOCATION_HOLDER, LocationHolder::new));
        matcher.add(new DefaultLayouter(DefaultLayouter.STICKER_HOLDER, StickerHolder::new));

        ActorSDK.sharedActor().getDelegate().configureChatViewHolders(matcher.getLayouters());


        this.messagesFragment = messagesFragment;
        this.context = context;
        ConversationVM conversationVM = messenger().getConversationVM(messagesFragment.getPeer());

        peer = messagesFragment.getPeer();

        readDate = conversationVM.getReadDate().get();
        receiveDate = conversationVM.getReceiveDate().get();

        BINDER.bind(conversationVM.getReadDate(), new ValueChangedListener<Long>() {
            @Override
            public void onChanged(Long val, Value<Long> valueModel) {
                if (val != readDate) {
                    for (int i = 0; i < displayList.getSize(); i++) {
                        long date = displayList.getItem(i).getSortDate();
                        if (date > readDate && date <= val) {
                            notifyItemChanged(i);
                        }
                        if (date <= readDate) {
                            break;
                        }
                    }
                    readDate = val;
                }
            }
        });
        BINDER.bind(conversationVM.getReceiveDate(), new ValueChangedListener<Long>() {
            @Override
            public void onChanged(Long val, Value<Long> valueModel) {
                if (val != receiveDate) {
                    for (int i = 0; i < displayList.getSize(); i++) {
                        long date = displayList.getItem(i).getSortDate();
                        if (date > receiveDate && date <= val) {
                            notifyItemChanged(i);
                        }
                        if (date <= receiveDate) {
                            break;
                        }
                    }
                    receiveDate = val;
                }
            }
        });
    }

    public Message[] getSelected() {
        return selected.values().toArray(new Message[selected.size()]);
    }

    public boolean isSelected(Message msg) {
        return selected.containsKey(msg.getRid());
    }

    public void setSelected(Message msg, boolean isSelected) {
        if (isSelected) {
            selected.put(msg.getRid(), msg);
        } else {
            selected.remove(msg.getRid());
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selected.size();
    }

    public void clearSelection() {
        selected.clear();
        notifyDataSetChanged();
    }

    public MessagesFragment getMessagesFragment() {
        return messagesFragment;
    }

    public long getFirstUnread() {
        return firstUnread;
    }

    public long getReadDate() {
        return readDate;
    }

    public long getReceiveDate() {
        return receiveDate;
    }

    public void setFirstUnread(long firstUnread) {
        this.firstUnread = firstUnread;
    }

    @Override
    public int getItemViewType(int position) {
        AbsContent content = getItem(position).getContent();
        return matcher.getMatchId(content);

    }


    @Override
    public AbsMessageViewHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        return matcher.onCreateViewHolder(viewType, this, viewGroup, peer);
    }

    @Override
    public void onBindViewHolder(AbsMessageViewHolder dialogHolder, int index, Message item) {
        Message prev = null;
        Message next = null;
        if (index > 1) {
            next = getItem(index - 1);
        }
        if (index < getItemCount() - 1) {
            prev = getItem(index + 1);
        }
        PreprocessedList list = ((PreprocessedList) getPreprocessedList());
        dialogHolder.bindData(item, prev, next, readDate, receiveDate, list.getPreprocessedData()[index]);
    }

    @Override
    public void onViewRecycled(AbsMessageViewHolder holder) {
        holder.unbind();
    }

    public ActorBinder getBinder() {
        return BINDER;
    }

}