/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import java.util.ArrayList;

import im.actor.core.api.rpc.RequestGetFileUrlBuilder;
import im.actor.core.api.rpc.ResponseGetFileUrlBuilder;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.Modules;
import im.actor.core.network.RpcCallback;
import im.actor.core.network.RpcException;
import im.actor.runtime.Crypto;
import im.actor.runtime.Log;
import im.actor.runtime.bser.DataOutput;
import im.actor.runtime.crypto.Cryptos;
import im.actor.runtime.crypto.primitives.hmac.HMAC;

/**
 * File's URL binder
 */
public class JsFilesModule extends AbsModule {

    private static final String TAG = "JsFilesModule";

    private static final ArrayList<String> SUPPORTED_ALGOS = new ArrayList<>();

    static {
        SUPPORTED_ALGOS.add("HMAC_SHA256");
    }

    private ArrayList<JsFileLoadedListener> listeners = new ArrayList<>();

    private boolean isLoaded = false;
    private String baseUrl;
    private String seed;
    private HMAC hmac;
    private int timeout;

    public JsFilesModule(final Modules modules) {
        super(modules);

        requestUrlBuilder();
    }

    /**
     * Registering Internal listener for binding updates
     *
     * @param listener listener for registration
     */
    public void registerListener(JsFileLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregistering Internal listener for binding updates
     *
     * @param listener listener for unregistering
     */
    public void unregisterListener(JsFileLoadedListener listener) {
        listeners.remove(listener);
    }

    /**
     * Getting URL for file if available
     *
     * @param id         file's id
     * @param accessHash file's accessHash
     * @return url for a file or null if not yet available
     */
    public String getFileUrl(long id, long accessHash) {
        requestUrlBuilderIfNeeded();
        if (isLoaded) {
            hmac.reset();
            DataOutput dataOutput = new DataOutput();
            dataOutput.writeBytes(seed.getBytes());
            dataOutput.writeBytes((id + "").getBytes());
            dataOutput.writeBytes((accessHash + "").getBytes());
            byte[] toHash = dataOutput.toByteArray();
            hmac.update(toHash, 0, toHash.length);
            byte[] sig = new byte[hmac.getDigestSize()];
            hmac.doFinal(sig, 0);
            String signature = seed + "_" + Crypto.hex(sig);
            return baseUrl + "/" + id + "?signature=" + signature;
        }

        return null;
    }

    private void requestUrlBuilderIfNeeded() {
        long currentTime = im.actor.runtime.Runtime.getCurrentSyncedTime();
        if (isLoaded && (timeout * 1000L < currentTime)) {
            requestUrlBuilder();
        }
    }

    private void requestUrlBuilder() {
        isLoaded = false;
        request(new RequestGetFileUrlBuilder(SUPPORTED_ALGOS), new RpcCallback<ResponseGetFileUrlBuilder>() {
            @Override
            public void onResult(ResponseGetFileUrlBuilder response) {

                isLoaded = true;
                baseUrl = response.getBaseUrl();
                seed = response.getSeed();
                hmac = Cryptos.HMAC_SHA256(response.getSignatureSecret());
                timeout = response.getTimeout();

                for (JsFileLoadedListener listener : listeners) {
                    listener.onUrlBuilderReady();
                }
            }

            @Override
            public void onError(RpcException e) {
                // Ignore
                Log.e(TAG, e);
            }
        });
    }
}