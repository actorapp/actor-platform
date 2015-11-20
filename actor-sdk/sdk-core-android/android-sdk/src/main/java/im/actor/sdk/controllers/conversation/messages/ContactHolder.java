package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.ContactContent;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.avatar.AvatarPlaceholderDrawable;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class ContactHolder extends MessageHolder {

    private ViewGroup mainContainer;
    private FrameLayout messageBubble;
    private TextView text;

    private ImageView contactAvatar;


    public ContactHolder(MessagesAdapter fragment, final View itemView) {
        super(fragment, itemView, false);

        mainContainer = (ViewGroup) itemView.findViewById(R.id.mainContainer);
        messageBubble = (FrameLayout) itemView.findViewById(R.id.fl_bubble);
        text = (TextView) itemView.findViewById(R.id.tv_text);
        text.setTextColor(ActorSDK.sharedActor().style.getConvTextColor());
        contactAvatar = (ImageView) itemView.findViewById(R.id.contact_avatar);

    }

    @Override
    protected void bindData(final Message message, boolean isUpdated, PreprocessedData preprocessedData) {

        ContactContent contact = (ContactContent) message.getContent();
        if (message.getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_in);
        }
        Drawable avatar;
        if (contact.getPhoto64() != null) {
            byte[] decodedByte = Base64.decode(contact.getPhoto64(), Base64.NO_WRAP);
            Bitmap b = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            avatar = getRoundedBitmapDrawable(itemView.getContext(), b);
        } else {
            avatar = new AvatarPlaceholderDrawable(contact.getName(), message.getSenderId(), 18, itemView.getContext());
        }
        contactAvatar.setBackgroundDrawable(avatar);

        text.setText(((PreprocessedTextData) preprocessedData).getSpannableString());

    }

    @Override
    public void onClick(Message currentMessage) {

        final ContactContent contact = (ContactContent) currentMessage.getContent();
        CharSequence[] items = new CharSequence[contact.getEmails().size() + contact.getPhones().size()];
        int i = 0;
        for (String phone : contact.getPhones()) {
            items[i++] = phone;
        }
        for (String email : contact.getEmails()) {
            items[i++] = email;
        }

        new AlertDialog.Builder(itemView.getContext())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        if (which + 1 <= contact.getPhones().size()) {

                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contact.getPhones().get(which), null));
                            itemView.getContext().startActivity(intent);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", contact.getEmails().get(which - contact.getPhones().size()), null));

                            itemView.getContext().startActivity(intent);
                        }
                    }
                }).show();
    }

    private RoundedBitmapDrawable getRoundedBitmapDrawable(Context context, Bitmap b) {
        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(context.getResources(), Bitmap.createScaledBitmap(b, Screen.dp(48), Screen.dp(48), false));
        d.setCornerRadius(d.getIntrinsicHeight() / 2);
        d.setAntiAlias(true);
        return d;
    }
}
