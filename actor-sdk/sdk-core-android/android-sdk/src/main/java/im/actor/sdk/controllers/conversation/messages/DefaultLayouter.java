package im.actor.sdk.controllers.conversation.messages;

import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.content.AbsMessageViewHolder;
import im.actor.sdk.controllers.conversation.view.BubbleContainer;
import im.actor.sdk.util.ViewUtils;

public class DefaultLayouter extends LambdaBubbleLayouter {

    public static final int TEXT_HOLDER = 0;
    public static final int SERVICE_HOLDER = 1;
    public static final int PHOTO_HOLDER = 2;
    public static final int VOICE_HOLDER = 4;
    public static final int DOCUMENT_HOLDER = 3;
    public static final int CONTACT_HOLDER = 5;
    public static final int LOCATION_HOLDER = 6;
    public static final int STICKER_HOLDER = 7;

    int id;
    int layoutId;

    public DefaultLayouter(int holderId, @NotNull ViewHolderCreator creator) {
        super(content -> false, creator);
        this.id = holderId;
    }


    @Override
    public boolean isMatch(AbsContent content) {
        for (HolderMapEntry e : holderMap) {
            if (e.getaClass().isAssignableFrom(content.getClass())) {
                layoutId = e.getLayoutId();
                return e.getId() == id;
            }
        }
        return false;
    }

    @Override
    public AbsMessageViewHolder onCreateViewHolder(MessagesAdapter adapter, ViewGroup root, Peer peer) {
        View view = ViewUtils.inflate(layoutId, root);
        if (!(view instanceof BubbleContainer)) {
            BubbleContainer container = new BubbleContainer(root.getContext());
            container.addView(view);
            view = container;
        }
        return creator.onCreateViewHolder(adapter, (ViewGroup) view, peer);
    }

    private static ArrayList<HolderMapEntry> holderMap;

    static {
        holderMap = new ArrayList<>();
        holderMap.add(new HolderMapEntry(TextContent.class, TEXT_HOLDER, R.layout.adapter_dialog_text));
        holderMap.add(new HolderMapEntry(ServiceContent.class, SERVICE_HOLDER, R.layout.adapter_dialog_service));
        holderMap.add(new HolderMapEntry(PhotoContent.class, PHOTO_HOLDER, R.layout.adapter_dialog_photo));
        holderMap.add(new HolderMapEntry(VideoContent.class, PHOTO_HOLDER, R.layout.adapter_dialog_photo));
        holderMap.add(new HolderMapEntry(AnimationContent.class, PHOTO_HOLDER, R.layout.adapter_dialog_photo));
        holderMap.add(new HolderMapEntry(VoiceContent.class, VOICE_HOLDER, R.layout.adapter_dialog_audio));
        holderMap.add(new HolderMapEntry(DocumentContent.class, DOCUMENT_HOLDER, R.layout.adapter_dialog_doc));
        holderMap.add(new HolderMapEntry(ContactContent.class, CONTACT_HOLDER, R.layout.adapter_dialog_contact));
        holderMap.add(new HolderMapEntry(LocationContent.class, LOCATION_HOLDER, R.layout.adapter_dialog_locaton));
        holderMap.add(new HolderMapEntry(StickerContent.class, STICKER_HOLDER, R.layout.adapter_dialog_sticker));
    }

    private static class HolderMapEntry {
        Class aClass;
        int id;
        int layoutId;

        public HolderMapEntry(Class aClass, int id, int layoutId) {
            this.aClass = aClass;
            this.id = id;
            this.layoutId = layoutId;
        }

        public Class getaClass() {
            return aClass;
        }

        public int getId() {
            return id;
        }

        public int getLayoutId() {
            return layoutId;
        }
    }

}