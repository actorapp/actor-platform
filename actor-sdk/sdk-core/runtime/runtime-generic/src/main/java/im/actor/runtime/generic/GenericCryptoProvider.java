package im.actor.runtime.generic;

import im.actor.runtime.*;
import im.actor.runtime.Runtime;
import im.actor.runtime.crypto.primitives.kuznechik.KuznechikFastEngine;

public class GenericCryptoProvider extends DefaultCryptoRuntime {

    private static boolean isLoaded = false;
    private static final Object LOCk = new Object();

    public GenericCryptoProvider() {
        Runtime.dispatch(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                byte[] tables = Assets.loadBinAsset("kuz_tables.bin");
                if (tables != null) {
                    KuznechikFastEngine.initDump(tables);
                } else {
                    KuznechikFastEngine.initCalc();
                }
                synchronized (LOCk) {
                    isLoaded = true;
                    LOCk.notifyAll();
                }
            }
        });
    }

    @Override
    public void waitForCryptoLoaded() {
        if (isLoaded) {
            return;
        }
        synchronized (LOCk) {
            if (isLoaded) {
                return;
            }
            try {
                LOCk.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
