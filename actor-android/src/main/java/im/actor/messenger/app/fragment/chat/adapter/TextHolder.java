package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.model.DialogType;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.storage.scheme.messages.types.TextMessage;
import im.actor.messenger.util.Screen;
import im.actor.messenger.util.TextUtils;

import static im.actor.messenger.core.Core.myUid;
import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class TextHolder extends BubbleHolder {

    private FrameLayout messageBubble;
    private TextView text;
    private TextView time;
    private TintImageView status;
    private int chatType;
    private int chatId;
    private int[] colors;

    private int waitColor;
    private int sentColor;
    private int deliveredColor;
    private int readColor;
    private int errorColor;

    protected TextHolder(int chatType, int chatId, MessagesFragment fragment, UiList<MessageModel> uiList) {
        super(fragment, uiList);
        this.chatType = chatType;
        this.chatId = chatId;
    }

    @Override
    public View init(MessageModel data, ViewGroup parent, Context context) {
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
    public void update(final MessageModel data, int position, boolean isUpdated, Context context) {
        if (data == null) {
            return;
        }

        if (data.getRaw().getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_in);
        }

        if (chatType == DialogType.TYPE_GROUP && data.getRaw().getSenderId() != myUid()) {
            String name;
            UserModel userModel = users().get(data.getRaw().getSenderId());
            if (userModel != null) {
                name = userModel.getName();
            } else {
                name = "???";
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(name);
            builder.setSpan(new ForegroundColorSpan(colors[Math.abs(data.getRaw().getSenderId()) % colors.length]), 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            builder.append("\n");
            builder.append(((TextMessage) data.getContent()).getText());
            text.setText(builder);
        } else {
            text.setText(((TextMessage) data.getContent()).getText());
        }

        Linkify.addLinks(text, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS |
                Linkify.WEB_URLS);

        if (data.getRaw().getSenderId() == myUid()) {
            status.setVisibility(View.VISIBLE);

            switch (data.getRaw().getMessageState()) {
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

        time.setText(TextUtils.formatTime(data.getRaw().getTime()));

        super.update(data, position, isUpdated, context);
    }

    @Override
    public void unbind() {

    }
}
