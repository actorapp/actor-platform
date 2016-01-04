package im.actor.sdk.controllers.conversation.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.JsonContent;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;
import im.actor.sdk.R;

public abstract class BaseJsonHolder extends MessageHolder {
    public BaseJsonHolder(MessagesAdapter adapter, ViewGroup viewGroup, int resourceId, boolean isFullSize) {
        super(adapter, inflate(resourceId, viewGroup), isFullSize);


    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        JSONObject json = ((JsonContent) message.getContent()).getJson();
        JSONObject data = null;
        try {
            data = json.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bindData(message, data, isUpdated, preprocessedData);
    }

    protected abstract void bindData(Message message, JSONObject data, boolean isUpdated, PreprocessedData preprocessedData);

    private static View inflate(int resourceId, ViewGroup viewGroup) {
        View base = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_holder, viewGroup, false);
        View content = LayoutInflater.from(viewGroup.getContext()).inflate(resourceId, viewGroup, false);
        ((FrameLayout) base.findViewById(R.id.custom_container)).addView(content);
        return base;
    }
}
