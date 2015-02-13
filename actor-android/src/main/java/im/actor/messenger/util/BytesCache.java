package im.actor.messenger.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;

public class BytesCache {

    public static BytesCache getInstance() {
        return instance;
    }

    private static BytesCache instance = new BytesCache("GlobalByteCache");

    private final int[] SIZES = new int[]{1, 2, 4, 8, 16, 64, 128, 3072, 4 * 1024, 20 * 1024, 40 * 1024};
    private final int MAX_SIZE = 40 * 1024;
    private final boolean TRACK_ALLOCATIONS = false;

    private HashMap<Integer, HashSet<byte[]>> fastBuffers = new HashMap<Integer, HashSet<byte[]>>();
    private HashSet<byte[]> mainFilter = new HashSet<byte[]>();
    private HashSet<byte[]> byteBuffer = new HashSet<byte[]>();
    private WeakHashMap<byte[], StackTraceElement[]> references = new WeakHashMap<byte[], StackTraceElement[]>();

    private final String TAG;

    public BytesCache(String logTag) {
        TAG = logTag;
        for (int i = 0; i < SIZES.length; i++) {
            fastBuffers.put(SIZES[i], new HashSet<byte[]>());
        }
    }

    public synchronized void put(byte[] data) {
        references.remove(data);

        if (mainFilter.add(data)) {
            for (Integer i : SIZES) {
                if (data.length == i) {
                    fastBuffers.get(i).add(data);
                    return;
                }
            }
            if (data.length <= MAX_SIZE) {
                return;
            }
            byteBuffer.add(data);
        }
    }

    public synchronized byte[] allocate(int minSize) {
        if (minSize <= MAX_SIZE) {
            for (int i = 0; i < SIZES.length; i++) {
                if (minSize < SIZES[i]) {
                    if (!fastBuffers.get(SIZES[i]).isEmpty()) {
                        Iterator<byte[]> interator = fastBuffers.get(SIZES[i]).iterator();
                        byte[] res = interator.next();
                        interator.remove();

                        mainFilter.remove(res);

                        if (TRACK_ALLOCATIONS) {
                            references.put(res, Thread.currentThread().getStackTrace());
                        }

                        return res;
                    }

                    return new byte[SIZES[i]];
                }
            }
        } else {
            byte[] res = null;
            for (byte[] cached : byteBuffer) {
                if (cached.length < minSize) {
                    continue;
                }
                if (res == null) {
                    res = cached;
                } else if (res.length > cached.length) {
                    res = cached;
                }
            }

            if (res != null) {
                byteBuffer.remove(res);
                mainFilter.remove(res);
                if (TRACK_ALLOCATIONS) {
                    references.put(res, Thread.currentThread().getStackTrace());
                }
                return res;
            }
        }

        return new byte[minSize];
    }
}
