package im.actor.model.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.model.droidkit.bser.Bser;
import im.actor.model.droidkit.bser.BserParser;
import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;
import im.actor.model.droidkit.bser.DataInput;
import static im.actor.model.droidkit.bser.Utils.*;
import java.io.IOException;
import im.actor.model.network.parser.*;
import java.util.List;
import java.util.ArrayList;

public class EncryptedTextContentV1 extends EncryptedContentV1 {

    private String text;

    public EncryptedTextContentV1(String text) {
        this.text = text;
    }

    public EncryptedTextContentV1() {

    }

    public int getHeader() {
        return 1;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.text = values.getString(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        if (this.text == null) {
            throw new IOException();
        }
        writer.writeString(2, this.text);
    }

    @Override
    public String toString() {
        String res = "struct EncryptedTextContentV1{";
        res += "}";
        return res;
    }

}
