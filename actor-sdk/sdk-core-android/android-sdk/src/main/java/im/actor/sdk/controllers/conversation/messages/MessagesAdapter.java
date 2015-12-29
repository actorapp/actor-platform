package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import im.actor.core.api.ApiJsonMessage;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.AbsContent;
import im.actor.core.entity.content.ContactContent;
import im.actor.core.entity.content.ContentConverter;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.JsonContent;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.ServiceContent;
import im.actor.core.entity.content.StickerContent;
import im.actor.core.entity.content.TextContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.entity.content.VoiceContent;
import im.actor.core.entity.content.internal.ContentRemoteContainer;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.sdk.controllers.fragment.ActorBinder;

public class MessagesAdapter extends BindedListAdapter<Message, MessageHolder> {

    private MessagesFragment messagesFragment;
    private Context context;
    private long firstUnread = -1;
    protected HashMap<Long, Message> selected = new HashMap<Long, Message>();
    private ActorBinder BINDER = new ActorBinder();

    public MessagesAdapter(BindedDisplayList<Message> displayList,
                           MessagesFragment messagesFragment, Context context) {
        super(displayList);

        this.messagesFragment = messagesFragment;
        this.context = context;
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

    public void setFirstUnread(long firstUnread) {
        this.firstUnread = firstUnread;
    }

    @Override
    public int getItemViewType(int position) {
        AbsContent content = getItem(position).getContent();
        for (int i = 0; i < AbsContent.getConverters().length; i++) {
            ContentConverter converter = AbsContent.getConverters()[i];
            if (content instanceof JsonContent) {
                if (converter.validate(content)) {
                    return 8 + i;
                }
            }

        }
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
            default:
                if (viewType - 8 >= 0 && viewType - 8 < AbsContent.getConverters().length) {
                    return ActorSDK.sharedActor().getDelegatedMessageViewHolder(viewType - 8, new ActorSDK.OnDelegateViewHolder<MessageHolder>() {
                        @Override
                        public MessageHolder onNotDelegated() {
                            return new UnsupportedHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
                        }
                    }, MessagesAdapter.this, viewGroup);
                } else {
                    return new UnsupportedHolder(MessagesAdapter.this, inflate(R.layout.adapter_dialog_text, viewGroup));
                }

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
        dialogHolder.bindData(item, prev, next, list.getPreprocessedData()[index]);
    }

    @Override
    public void onViewRecycled(MessageHolder holder) {
        holder.unbind();
    }

    public ActorBinder getBinder() {
        return BINDER;
    }
}