/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.android;

import android.content.res.Resources;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import im.actor.runtime.HttpRuntime;
import im.actor.runtime.Log;
import im.actor.runtime.http.HTTPError;
import im.actor.runtime.http.HTTPResponse;
import im.actor.runtime.promise.Promise;
import okio.Buffer;

public class AndroidHttpProvider implements HttpRuntime {

    private static final String TAG = "AndroidHTTP";

    private final OkHttpClient client = new OkHttpClient();

    private final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");

    public AndroidHttpProvider() {
        Resources resources = AndroidContext.getContext().getResources();
        try {
            String cert = resources.getString(resources.getIdentifier("trusted_pem", "string", AndroidContext.getContext().getPackageName()));
            SSLContext sslContext = sslContextForTrustedCertificates(new Buffer()
                    .writeUtf8(cert)
                    .inputStream());
            client.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (Resources.NotFoundException e) {
            // Just Ignore
        }

        try {
            final String trustHostname = resources.getString(resources.getIdentifier("trusted_hostname", "string", AndroidContext.getContext().getPackageName()));
            client.setHostnameVerifier((hostname, session) -> hostname.equals(trustHostname));
        } catch (Resources.NotFoundException e) {
            // Just Ignore
        }
    }

    public SSLContext sslContextForTrustedCertificates(InputStream in) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }

            // Put the certificates a key store.
            char[] password = "password".toCharArray(); // Any password will work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            // Wrap it up in an SSL context.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public Promise<HTTPResponse> getMethod(String url, int startOffset, int size, int totalSize) {
        return new Promise<>(resolver -> {
            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Range", "bytes=" + startOffset + "-" + (startOffset + size))
                    .build();
            Log.d(TAG, "Downloading part: " + request.toString());
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "Downloading part error: " + request.toString());
                    e.printStackTrace();
                    // TODO: Better error?
                    resolver.error(new HTTPError(0));
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Log.d(TAG, "Downloading part response: " + request.toString() + " -> " + response.toString());
                    if (response.code() >= 200 && response.code() < 300) {
                        resolver.result(new HTTPResponse(response.code(), response.body().bytes()));
                    } else {
                        resolver.error(new HTTPError(response.code()));
                    }
                }
            });
        });
    }

    @Override
    public Promise<HTTPResponse> putMethod(String url, byte[] contents) {
        return new Promise<>(resolver -> {
            final Request request = new Request.Builder()
                    .url(url)
                    .method("PUT", RequestBody.create(MEDIA_TYPE, contents))
                    .build();
            Log.d(TAG, "Uploading part: " + request.toString());
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "Uploading part error: " + request.toString());
                    e.printStackTrace();
                    // TODO: Better error?
                    resolver.error(new HTTPError(0));
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    Log.d(TAG, "Upload part response: " + request.toString() + " -> " + response.toString());
                    if (response.code() >= 200 && response.code() < 300) {
                        resolver.result(new HTTPResponse(response.code(), null));
                    } else {
                        resolver.error(new HTTPError(response.code()));
                    }
                }
            });
        });
    }
}
