package im.actor;

import android.graphics.Color;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Random;

import im.actor.core.entity.Message;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.messages.BaseCustomHolder;
import im.actor.sdk.controllers.conversation.messages.MessageHolder;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.PreprocessedData;
import im.actor.sdk.controllers.conversation.messages.PreprocessedTextData;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.TintImageView;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class CustomHolder extends BaseCustomHolder {

    TextView text;

    public CustomHolder(MessagesAdapter adapter, ViewGroup viewGroup, int id, boolean isFullSize) {
        super(adapter, viewGroup, id, isFullSize);
        text = (TextView) itemView.findViewById(R.id.tv_text);

    }

    @Override
    protected void bindData(Message message, boolean isUpdated, PreprocessedData preprocessedData) {
        text.setText(((Application.Custom) message.getContent()).getText());
    }
}
