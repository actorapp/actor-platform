package im.actor.sdk.core.audio;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ex3ndr on 17.03.14.
 */
public class VoiceBuffers {

    private static VoiceBuffers instance;

    public static synchronized VoiceBuffers getInstance() {
        if (instance == null) {
            instance = new VoiceBuffers();
        }
        return instance;
    }

    private final HashMap<Integer, ArrayList<byte[]>> freeBuffers = new HashMap<Integer, ArrayList<byte[]>>();

    private VoiceBuffers() {

    }

    public byte[] obtainBuffer(int size) {
        synchronized (freeBuffers) {
            if (freeBuffers.containsKey(size)) {
                ArrayList<byte[]> b = freeBuffers.get(size);
                if (b.size() > 0) {
                    return b.remove(0);
                }
            }
        }

        return new byte[size];
    }

    public void releaseBuffer(byte[] b) {
        synchronized (freeBuffers) {
            if (freeBuffers.containsKey(b.length)) {
                freeBuffers.get(b.length).add(b);
                return;
            } else {
                ArrayList<byte[]> res = new ArrayList<byte[]>();
                res.add(b);
                freeBuffers.put(b.length, res);
            }
        }
    }
}