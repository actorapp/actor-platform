package im.actor.sdk.controllers.conversation.messages;

import android.view.LayoutInflater;
import android.view.ViewGroup;


/**
 * Created by root on 12/29/15.
 */
public abstract class BaseCustomHolder extends MessageHolder {
    public BaseCustomHolder(MessagesAdapter adapter, ViewGroup viewGroup, int resourceId, boolean isFullSize) {
        super(adapter, LayoutInflater
                .from(viewGroup.getContext())
                .inflate(resourceId, viewGroup, false), isFullSize);


    }

}
