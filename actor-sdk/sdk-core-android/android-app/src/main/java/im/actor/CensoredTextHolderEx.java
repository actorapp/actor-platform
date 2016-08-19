package im.actor;

import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;

import im.actor.core.entity.Message;
import im.actor.core.entity.Peer;
import im.actor.sdk.controllers.conversation.messages.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.content.TextHolder;

public class CensoredTextHolderEx extends TextHolder {
    public CensoredTextHolderEx(MessagesAdapter adapter, View itemView, Peer peer) {
        super(adapter, itemView, peer);
    }

    private static ArrayList<String> badWords;

    static {
        badWords = new ArrayList<>();
        badWords.add("fuck");
        badWords.add("poke");
        badWords.add("poké");
    }

    @Override
    public void bindRawText(CharSequence rawText, long readDate, long receiveDate, Spannable reactions, Message message, boolean isItalic) {
        for (String s : badWords) {
            rawText = rawText.toString().replaceAll("/*(?i)" + s + "/*", new String(new char[s.length()]).replace('\0', '*'));
        }
        super.bindRawText(rawText, readDate, receiveDate, reactions, message, isItalic);
    }


}
