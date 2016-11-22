package im.actor.sdk.intents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.bser.Bser;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class ShareAction extends BserObject {

    public static ShareAction fromBytes(byte[] data) throws IOException {
        return Bser.parse(new ShareAction(), data);
    }

    private String text;
    private List<String> uris = new ArrayList<>();
    private Integer userId;
    private String forwardText;
    private String forwardTextRaw;
    private byte[] docContent;

    public ShareAction(String text) {
        this.text = text;
    }

    public ShareAction(byte[] docContent) {
        this.docContent = docContent;
    }

    public ShareAction(String forwardText, String forwardTextRaw) {
        this.forwardText = forwardText;
        this.forwardTextRaw = forwardTextRaw;
    }

    public ShareAction(List<String> uris) {
        this.uris = uris;
    }

    public ShareAction(int userId) {
        this.userId = userId;
    }

    private ShareAction() {

    }

    public String getText() {
        return text;
    }

    public List<String> getUris() {
        return uris;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getForwardText() {
        return forwardText;
    }

    public String getForwardTextRaw() {
        return forwardTextRaw;
    }

    public byte[] getDocContent() {
        return docContent;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        text = values.optString(1);
        uris = values.getRepeatedString(2);
        userId = values.optInt(3);
        forwardText = values.optString(4);
        forwardTextRaw = values.optString(5);
        docContent = values.optBytes(6);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (text != null) {
            writer.writeString(1, text);
        }
        if (uris != null) {
            writer.writeRepeatedString(2, uris);
        }
        if (userId != null) {
            writer.writeInt(3, userId);
        }
        if (forwardText != null) {
            writer.writeString(4, forwardText);
        }
        if (forwardTextRaw != null) {
            writer.writeString(5, forwardTextRaw);
        }
        if (docContent != null) {
            writer.writeBytes(6, docContent);
        }
    }
}
