package im.actor.messenger.app.fragment.chat.recycler;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.TextContent;
import im.actor.model.log.Log;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class TextHolder extends MessageHolder {

    private BubbleContainer messageBubbleContainer;
    private FrameLayout messageBubble;
    private TextView text;
    private TextView time;
    private TintImageView status;
    private int[] colors;

    private int waitColor;
    private int sentColor;
    private int deliveredColor;
    private int readColor;
    private int errorColor;

    public TextHolder(MessagesFragment fragment, View itemView) {
        super(fragment, itemView, false);

        messageBubbleContainer = (BubbleContainer) itemView;
        messageBubble = (FrameLayout) itemView.findViewById(R.id.fl_bubble);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        text.setTypeface(Fonts.regular());
        time = (TextView) itemView.findViewById(R.id.tv_time);
        time.setTypeface(Fonts.regular());
        status = (TintImageView) itemView.findViewById(R.id.stateIcon);

        colors = new int[]{
                itemView.getResources().getColor(R.color.placeholder_0),
                itemView.getResources().getColor(R.color.placeholder_1),
                itemView.getResources().getColor(R.color.placeholder_2),
                itemView.getResources().getColor(R.color.placeholder_3),
                itemView.getResources().getColor(R.color.placeholder_4),
                itemView.getResources().getColor(R.color.placeholder_5),
                itemView.getResources().getColor(R.color.placeholder_6),
        };

        waitColor = itemView.getResources().getColor(R.color.conv_state_pending);
        sentColor = itemView.getResources().getColor(R.color.conv_state_sent);
        deliveredColor = itemView.getResources().getColor(R.color.conv_state_delivered);
        readColor = itemView.getResources().getColor(R.color.conv_state_read);
        errorColor = itemView.getResources().getColor(R.color.conv_state_error);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated) {
        Log.d("TextHolder", "bindData " + hashCode() + " #" + message.getListId() + " " + isUpdated);
        if (message.getSenderId() == myUid()) {
            messageBubbleContainer.makeOutboundBubble();
            messageBubble.setBackgroundResource(R.drawable.bubble_text_out);
        } else {
            messageBubbleContainer.makeInboundBubble(false, message.getSenderId());
            messageBubble.setBackgroundResource(R.drawable.bubble_text_in);
        }

        if (getPeer().getPeerType() == PeerType.GROUP && message.getSenderId() != myUid()) {
            String name;
            UserVM userModel = users().get(message.getSenderId());
            if (userModel != null) {
                name = userModel.getName().get();
            } else {
                name = "???";
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(name);
            builder.setSpan(new ForegroundColorSpan(colors[Math.abs(message.getSenderId()) % colors.length]), 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.append("\n");
            builder.append(((TextContent) message.getContent()).getText());
            text.setText(builder);
        } else {
            text.setText(((TextContent) message.getContent()).getText());
        }

        Linkify.addLinks(text, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS |
                Linkify.WEB_URLS);

        if (message.getSenderId() == myUid()) {
            status.setVisibility(View.VISIBLE);

            switch (message.getMessageState()) {
                case SENT:
                    status.setResource(R.drawable.msg_check_1);
                    status.setTint(sentColor);
                    break;
                case RECEIVED:
                    status.setResource(R.drawable.msg_check_2);
                    status.setTint(deliveredColor);
                    break;
                case READ:
                    status.setResource(R.drawable.msg_check_2);
                    status.setTint(readColor);
                    break;
                default:
                case PENDING:
                    status.setResource(R.drawable.msg_clock);
                    status.setTint(waitColor);
                    break;
                case ERROR:
                    status.setResource(R.drawable.msg_error);
                    status.setTint(errorColor);
                    break;
            }
        } else {
            status.setVisibility(View.GONE);
        }

        time.setText(TextUtils.formatTime(message.getDate()));
    }
}
