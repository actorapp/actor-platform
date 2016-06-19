package im.actor.core.api;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public abstract class ApiWebRTCSignaling extends BserObject {
    public static ApiWebRTCSignaling fromBytes(byte[] src) throws IOException {
        BserValues values = new BserValues(BserParser.deserialize(new DataInput(src, 0, src.length)));
        int key = values.getInt(1);
        byte[] content = values.getBytes(2);
        switch(key) { 
            case 21: return Bser.parse(new ApiAdvertiseSelf(), content);
            case 26: return Bser.parse(new ApiAdvertiseMaster(), content);
            case 3: return Bser.parse(new ApiCandidate(), content);
            case 4: return Bser.parse(new ApiOffer(), content);
            case 5: return Bser.parse(new ApiAnswer(), content);
            case 28: return Bser.parse(new ApiMediaStreamsUpdated(), content);
            case 8: return Bser.parse(new ApiNeedOffer(), content);
            case 24: return Bser.parse(new ApiNegotinationSuccessful(), content);
            case 22: return Bser.parse(new ApiEnableConnection(), content);
            case 25: return Bser.parse(new ApiOnRenegotiationNeeded(), content);
            case 6: return Bser.parse(new ApiCloseSession(), content);
            case 20: return Bser.parse(new ApiNeedDisconnect(), content);
            default: return new ApiWebRTCSignalingUnsupported(key, content);
        }
    }
    public abstract int getHeader();

    public byte[] buildContainer() throws IOException {
        DataOutput res = new DataOutput();
        BserWriter writer = new BserWriter(res);
        writer.writeInt(1, getHeader());
        writer.writeBytes(2, toByteArray());
        return res.toByteArray();
    }

}
