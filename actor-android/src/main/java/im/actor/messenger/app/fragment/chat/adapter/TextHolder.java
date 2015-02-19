package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.TextContent;
import im.actor.model.viewmodel.UserVM;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.core.Core.users;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class TextHolder extends BubbleHolder {

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

    protected TextHolder(Peer peer, MessagesFragment fragment, UiList<Message> uiList) {
        super(peer, fragment, uiList);
    }

    @Override
    public View init(Message data, ViewGroup parent, Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleContainer v = (BubbleContainer) inflater.inflate(R.layout.adapter_dialog_text, parent, false);

        messageBubble = (FrameLayout) v.findViewById(R.id.fl_bubble);
        text = (TextView) v.findViewById(R.id.tv_text);
        text.setTypeface(Fonts.regular());
        time = (TextView) v.findViewById(R.id.tv_time);
        time.setTypeface(Fonts.regular());
        status = (TintImageView) v.findViewById(R.id.stateIcon);

        colors = new int[]{
                context.getResources().getColor(R.color.placeholder_0),
                context.getResources().getColor(R.color.placeholder_1),
                context.getResources().getColor(R.color.placeholder_2),
                context.getResources().getColor(R.color.placeholder_3),
                context.getResources().getColor(R.color.placeholder_4),
                context.getResources().getColor(R.color.placeholder_5),
                context.getResources().getColor(R.color.placeholder_6),
        };

        waitColor = context.getResources().getColor(R.color.conv_state_pending);
        sentColor = context.getResources().getColor(R.color.conv_state_sent);
        deliveredColor = context.getResources().getColor(R.color.conv_state_delivered);
        readColor = context.getResources().getColor(R.color.conv_state_read);
        errorColor = context.getResources().getColor(R.color.conv_state_error);

        initBubbleHolder(v, false);

        return v;
    }

    @Override
    public void update(final Message data, int position, boolean isUpdated, Context context) {
        if (data == null) {
            return;
        }

        if (data.getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_in);
        }

        if (peer.getPeerType() == PeerType.GROUP && data.getSenderId() != myUid()) {
            String name;
            UserVM userModel = users().get(data.getSenderId());
            if (userModel != null) {
                name = userModel.getName().get();
            } else {
                name = "???";
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(name);
            builder.setSpan(new ForegroundColorSpan(colors[Math.abs(data.getSenderId()) % colors.length]), 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.append("\n");
            builder.append(((TextContent) data.getContent()).getText());
            text.setText(builder);
        } else {
            text.setText(((TextContent) data.getContent()).getText());
        }

        Linkify.addLinks(text, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS |
                Linkify.WEB_URLS);

        if (data.getSenderId() == myUid()) {
            status.setVisibility(View.VISIBLE);

            switch (data.getMessageState()) {
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

        time.setText(TextUtils.formatTime(data.getDate()));

        super.update(data, position, isUpdated, context);
    }

    @Override
    public void unbind() {

    }
}
