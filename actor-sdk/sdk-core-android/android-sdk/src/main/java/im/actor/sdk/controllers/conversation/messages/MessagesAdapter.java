package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.AnimationContent;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.entity.content.VoiceContent;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
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
import im.actor.sdk.util.ViewUtils;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MessagesAdapter extends BindedListAdapter<Message, AbsMessageViewHolder> {

    public static final int TEXT_CONTENT = 0;
    public static final int SERVICE_CONTENT = 1;
    public static final int PHOTO_CONTENT = 2;
    public static final int VOICE_CONTENT = 4;
    public static final int DOCUMENT_CONTENT = 3;
    public static final int CONTACT_CONTENT = 5;
    public static final int LOCATION_CONTENT = 6;
    public static final int STICKER_CONTENT = 7;

    private MessagesFragment messagesFragment;
    private ActorBinder BINDER = new ActorBinder();

    private Context context;
    private long firstUnread = -SERVICE_CONTENT;
    private long readDate;
    private long receiveDate;
    private Peer peer;
    private ViewHolderMatcher matcher;

    private HashMap<Long, Message> selected = new HashMap<>();

    private static ArrayList<HolderMapEntry> holderMap;

    static {
        holderMap = new ArrayList<>();
        holderMap.add(new HolderMapEntry(TextContent.class, TEXT_CONTENT));
        holderMap.add(new HolderMapEntry(ServiceContent.class, SERVICE_CONTENT));
        holderMap.add(new HolderMapEntry(PhotoContent.class, PHOTO_CONTENT));
        holderMap.add(new HolderMapEntry(VideoContent.class, PHOTO_CONTENT));
        holderMap.add(new HolderMapEntry(AnimationContent.class, PHOTO_CONTENT));
        holderMap.add(new HolderMapEntry(VoiceContent.class, VOICE_CONTENT));
        holderMap.add(new HolderMapEntry(DocumentContent.class, DOCUMENT_CONTENT));
        holderMap.add(new HolderMapEntry(ContactContent.class, CONTACT_CONTENT));
        holderMap.add(new HolderMapEntry(LocationContent.class, LOCATION_CONTENT));
        holderMap.add(new HolderMapEntry(StickerContent.class, STICKER_CONTENT));
    }

    private static class HolderMapEntry {
        Class aClass;
        int id;

        public HolderMapEntry(Class aClass, int id) {
            this.aClass = aClass;
            this.id = id;
        }

        public Class getaClass() {
            return aClass;
        }

        public int getId() {
            return id;
        }
    }


    public MessagesAdapter(final BindedDisplayList<Message> displayList,
                           MessagesFragment messagesFragment, Context context) {
        super(displayList);

        matcher = new ViewHolderMatcher();

        matcher.add(new DefaultLayouter(TEXT_CONTENT, R.layout.adapter_dialog_text, TextHolder::new));
        matcher.add(new DefaultLayouter(SERVICE_CONTENT, R.layout.adapter_dialog_service, ServiceHolder::new));
        matcher.add(new DefaultLayouter(PHOTO_CONTENT, R.layout.adapter_dialog_photo, PhotoHolder::new));
        matcher.add(new DefaultLayouter(VOICE_CONTENT, R.layout.adapter_dialog_audio, AudioHolder::new));
        matcher.add(new DefaultLayouter(DOCUMENT_CONTENT, R.layout.adapter_dialog_doc, DocHolder::new));
        matcher.add(new DefaultLayouter(CONTACT_CONTENT, R.layout.adapter_dialog_contact, ContactHolder::new));
        matcher.add(new DefaultLayouter(LOCATION_CONTENT, R.layout.adapter_dialog_locaton, LocationHolder::new));
        matcher.add(new DefaultLayouter(STICKER_CONTENT, R.layout.adapter_dialog_sticker, StickerHolder::new));

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
        if (index > SERVICE_CONTENT) {
            next = getItem(index - SERVICE_CONTENT);
        }
        if (index < getItemCount() - SERVICE_CONTENT) {
            prev = getItem(index + SERVICE_CONTENT);
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

    private class DefaultLayouter implements BubbleLayouter {
        int id;
        int layoutId;
        HolderCreator holderCreator;

        public DefaultLayouter(int id, int layoutId, HolderCreator holderCreator) {
            this.id = id;
            this.layoutId = layoutId;
            this.holderCreator = holderCreator;
        }

        @Override
        public boolean isMatch(AbsContent content) {
            for (HolderMapEntry e : holderMap) {
                if (e.getaClass().isAssignableFrom(content.getClass())) {
                    return e.getId() == id;
                }
            }
            return false;
        }

        @Override
        public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
            return holderCreator.createHolder(adapter, ViewUtils.inflate(layoutId, root), peer);
        }
    }

    private interface HolderCreator {
        AbsMessageViewHolder createHolder(MessagesAdapter adapter, View view, Peer peer);
    }

}