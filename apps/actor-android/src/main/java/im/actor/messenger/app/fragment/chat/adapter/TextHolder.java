package im.actor.messenger.app.fragment.chat.adapter;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.SmileProcessor;
import im.actor.messenger.app.fragment.chat.MessagesAdapter;
import im.actor.messenger.app.keyboard.emoji.smiles.SmilesListener;
import im.actor.messenger.app.util.TextUtils;
import im.actor.messenger.app.view.Fonts;
import im.actor.messenger.app.view.TintImageView;
import im.actor.model.entity.Message;
import im.actor.model.entity.PeerType;
import im.actor.model.entity.content.TextContent;
import im.actor.model.viewmodel.UserVM;
import in.uncod.android.bypass.Bypass;

import static im.actor.messenger.app.Core.myUid;
import static im.actor.messenger.app.Core.users;
import static im.actor.messenger.app.emoji.SmileProcessor.emoji;

/**
 * Created by ex3ndr on 10.09.14.
 */
public class TextHolder extends MessageHolder {

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

    private SmilesListener smilesListener;
    Bypass bypass = new Bypass();

    public TextHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, false);

        messageBubble = (FrameLayout) itemView.findViewById(R.id.fl_bubble);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
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
    protected void bindData(final Message message, boolean isUpdated) {

        if (message.getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.bubble_text_in);
        }


        CharSequence spannedText;

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
            builder.append(bypass.markdownToSpannable(((TextContent) message.getContent()).getText(), false));
            //builder.append(((TextContent) message.getContent()).getText());
            spannedText = builder;
        } else {
            spannedText = bypass.markdownToSpannable(((TextContent) message.getContent()).getText(), false);
            //spannedText = ((TextContent) message.getContent()).getText();
        }



        Editable spannedTextEditable = (Editable) spannedText;
        URLSpan[] urlSpans = spannedTextEditable.getSpans(0, spannedTextEditable.length(), URLSpan.class);
        if(urlSpans.length>0){
            int start;
            int end;
            int prevEnd = 0;
            Spannable toLinkyfy;
            for (int i = 0; i < urlSpans.length; i++) {
                start = spannedTextEditable.getSpanStart(urlSpans[i]);
                end = spannedTextEditable.getSpanEnd(urlSpans[i]);
                if(start>spannedText.length()-1)continue;
                toLinkyfy = (Spannable) spannedText.subSequence(prevEnd , start);
                Linkify.addLinks(toLinkyfy, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                spannedTextEditable.replace(prevEnd, start, toLinkyfy);
                prevEnd = end;
            }
            toLinkyfy = (Spannable) spannedText.subSequence(prevEnd, spannedTextEditable.length());
            Linkify.addLinks(toLinkyfy, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
            spannedTextEditable.replace(prevEnd, spannedTextEditable.length(), toLinkyfy);
            spannedText = spannedTextEditable;
        }else{
            Linkify.addLinks((Spannable) spannedText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
        }

        if (emoji().containsEmoji(spannedText)) {
            if (emoji().isLoaded()) {
                spannedText = emoji().processEmojiCompatMutable(spannedText, SmileProcessor.CONFIGURATION_BUBBLES);
            } else {
                final CharSequence finalSpannedText = spannedText;
                if(smilesListener !=null){
                    emoji().unregisterListener(smilesListener);
                }
                smilesListener = new SmilesListener() {
                    @Override
                    public void onSmilesUpdated(boolean completed) {
                        text.setText(emoji().processEmojiCompatMutable(finalSpannedText, SmileProcessor.CONFIGURATION_BUBBLES));
                        emoji().unregisterListener(this);
                    }
                };
                emoji().registerListener(smilesListener);
            }
        }

        text.setText(spannedText);

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

    @Override
    public void unbind() {
        super.unbind();
        emoji().unregisterListener(smilesListener);
    }

}
