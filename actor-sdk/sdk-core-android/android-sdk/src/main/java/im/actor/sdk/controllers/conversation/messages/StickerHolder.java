package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.StickerContent;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.util.Strings;
import im.actor.sdk.view.TintImageView;
import im.actor.sdk.view.emoji.stickers.StickerView;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class StickerHolder extends MessageHolder {


    private Context context;

    private final int COLOR_PENDING;
    private final int COLOR_SENT;
    private final int COLOR_RECEIVED;
    private final int COLOR_READ;
    private final int COLOR_ERROR;
    private TextView time;
    private TintImageView stateIcon;


    // Content Views
    private StickerView sticker;

    public StickerHolder(MessagesAdapter fragment, View itemView) {

        super(fragment, itemView, false);
        this.context = fragment.getMessagesFragment().getActivity();

        COLOR_PENDING = ActorSDK.sharedActor().style.getConvMediaStatePendingColor();
        COLOR_SENT = ActorSDK.sharedActor().style.getConvMediaStateSentColor();
        COLOR_RECEIVED = ActorSDK.sharedActor().style.getConvMediaStateDeliveredColor();
        COLOR_READ = ActorSDK.sharedActor().style.getConvMediaStateReadColor();
        COLOR_ERROR = ActorSDK.sharedActor().style.getConvMediaStateErrorColor();

        sticker = (StickerView) itemView.findViewById(R.id.sticker);
        time = (TextView) itemView.findViewById(R.id.time);
        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);

        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, boolean isNewMessage, PreprocessedData preprocessedData) {
        StickerContent content = (StickerContent) message.getContent();
        sticker.bind(content.getSticker(), StickerView.STICKER_FULL);

        int w = ((StickerContent) message.getContent()).getSticker().getWidth512();
        int h = ((StickerContent) message.getContent()).getSticker().getHeight512();

        int maxHeight = context.getResources().getDisplayMetrics().heightPixels - Screen.dp(96 + 32);
        maxHeight = Math.min(Screen.dp(200), maxHeight);
        int maxWidth = context.getResources().getDisplayMetrics().widthPixels - Screen.dp(32 + 48);
        maxWidth = Math.min(Screen.dp(200), maxWidth);

        float scale = Math.min(maxWidth / (float) w, maxHeight / (float) h);

        int bubbleW = (int) (scale * w);
        int bubbleH = (int) (scale * h);
        ViewGroup.LayoutParams params = sticker.getLayoutParams();
        params.height = bubbleH;
        params.width = bubbleW;


        // Update state
        if (message.getSenderId() == myUid()) {
            stateIcon.setVisibility(View.VISIBLE);
            switch (message.getMessageState()) {
                case ERROR:
                    stateIcon.setResource(R.drawable.msg_error);
                    stateIcon.setTint(COLOR_ERROR);
                    break;
                default:
                case PENDING:
                    stateIcon.setResource(R.drawable.msg_clock);
                    stateIcon.setTint(COLOR_PENDING);
                    break;
                case READ:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(COLOR_READ);
                    break;
                case RECEIVED:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(COLOR_RECEIVED);
                    break;
                case SENT:
                    stateIcon.setResource(R.drawable.msg_check_1);
                    stateIcon.setTint(COLOR_SENT);
                    break;
            }
        } else {
            stateIcon.setVisibility(View.GONE);
        }

        // Update time
        time.setText(Strings.formatTime(message.getDate()));

    }


    @Override
    public void unbind() {
        super.unbind();

        sticker.unbind();
    }


}
