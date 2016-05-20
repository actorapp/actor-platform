package im.actor.sdk.controllers.conversation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.entity.content.VoiceContent;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.sdk.controllers.conversation.messages.AudioHolder;
import im.actor.sdk.controllers.conversation.messages.ContactHolder;
import im.actor.sdk.controllers.conversation.messages.DocHolder;
import im.actor.sdk.controllers.conversation.messages.LocationHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.PhotoHolder;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedList;
import im.actor.sdk.controllers.conversation.messages.ServiceHolder;
import im.actor.sdk.controllers.conversation.messages.StickerHolder;
import im.actor.sdk.controllers.conversation.messages.TextHolder;
import im.actor.sdk.controllers.conversation.messages.UnsupportedHolder;
import im.actor.sdk.controllers.fragment.ActorBinder;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class MessagesAdapter extends BindedListAdapter<Message, MessageHolder> {

    private MessagesFragment messagesFragment;
    private ActorBinder BINDER = new ActorBinder();

    private Context context;
    private long firstUnread = -1;
    private long readDate;
    private long receiveDate;

    private HashMap<Long, Message> selected = new HashMap<>();

    public MessagesAdapter(final BindedDisplayList<Message> displayList,
                           MessagesFragment messagesFragment, Context context) {
        super(displayList);

        this.messagesFragment = messagesFragment;
        this.context = context;
        ConversationVM conversationVM = messenger().getConversationVM(messagesFragment.getPeer());

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

        if (content instanceof TextContent) {
            return 0;
        } else if (content instanceof ServiceContent) {
            return 1;
        } else if (content instanceof PhotoContent) {
            return 2;
        } else if (content instanceof VideoContent) {
            return 2;
        } else if (content instanceof VoiceContent) {
            return 4;
        } else if (content instanceof DocumentContent) {
            return 3;
        } else if (content instanceof ContactContent) {
            return 5;
        } else if (content instanceof LocationContent) {
            return 6;
        } else if (content instanceof StickerContent) {
            return 7;
        } else if (content instanceof JsonContent) {
            try {
                String dataType = new JSONObject(((JsonContent) content).getRawJson()).getString("dataType");
                return dataType.hashCode();
            } catch (JSONException e) {
                return -1;
            }
        }
        return -1;
    }

    protected View inflate(int id, ViewGroup viewGroup) {
        return LayoutInflater
                .from(context)
                .inflate(id, viewGroup, false);
    }

    @Override
    public MessageHolder onCreateViewHolder(final ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                return ActorSDK.sharedActor().getDelegatedViewHolder(TextHolder.class, new ActorSDK.OnDelegateViewHolder<TextHolder>() {
                    @Override
                    public TextHolder onNotDelegated() {
                        return new TextHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
            case 1:
                return ActorSDK.sharedActor().getDelegatedViewHolder(ServiceHolder.class, new ActorSDK.OnDelegateViewHolder<ServiceHolder>() {
                    @Override
                    public ServiceHolder onNotDelegated() {
                        return new ServiceHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_service, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_service, viewGroup));
            case 2:
                return ActorSDK.sharedActor().getDelegatedViewHolder(PhotoHolder.class, new ActorSDK.OnDelegateViewHolder<PhotoHolder>() {
                    @Override
                    public PhotoHolder onNotDelegated() {
                        return new PhotoHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_photo, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_photo, viewGroup));
            case 3:
                return ActorSDK.sharedActor().getDelegatedViewHolder(DocHolder.class, new ActorSDK.OnDelegateViewHolder<DocHolder>() {
                    @Override
                    public DocHolder onNotDelegated() {
                        return new DocHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_doc, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_doc, viewGroup));
            case 4:
                return ActorSDK.sharedActor().getDelegatedViewHolder(AudioHolder.class, new ActorSDK.OnDelegateViewHolder<AudioHolder>() {
                    @Override
                    public AudioHolder onNotDelegated() {
                        return new AudioHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_audio, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_audio, viewGroup));
            case 5:
                return ActorSDK.sharedActor().getDelegatedViewHolder(ContactHolder.class, new ActorSDK.OnDelegateViewHolder<ContactHolder>() {
                    @Override
                    public ContactHolder onNotDelegated() {
                        return new ContactHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_contact, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_contact, viewGroup));
            case 6:
                return ActorSDK.sharedActor().getDelegatedViewHolder(LocationHolder.class, new ActorSDK.OnDelegateViewHolder<LocationHolder>() {
                    @Override
                    public LocationHolder onNotDelegated() {
                        return new LocationHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_locaton, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_locaton, viewGroup));
            case 7:
                return ActorSDK.sharedActor().getDelegatedViewHolder(StickerHolder.class, new ActorSDK.OnDelegateViewHolder<StickerHolder>() {
                    @Override
                    public StickerHolder onNotDelegated() {
                        return new StickerHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_sticker, viewGroup));
                    }
                }, MessagesAdapter.this, inflate(R.layout.adapter_dialog_sticker, viewGroup));
            case -1:
                return new UnsupportedHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
            default:
                return ActorSDK.sharedActor().getDelegatedCustomMessageViewHolder(viewType, new ActorSDK.OnDelegateViewHolder<MessageHolder>() {
                    @Override
                    public MessageHolder onNotDelegated() {
                        return new UnsupportedHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
                    }
                }, MessagesAdapter.this, viewGroup);

        }
    }

    @Override
    public void onBindViewHolder(MessageHolder dialogHolder, int index, Message item) {
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
    public void onViewRecycled(MessageHolder holder) {
        holder.unbind();
    }

    public ActorBinder getBinder() {
        return BINDER;
    }
}