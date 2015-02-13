package im.actor.messenger.core.actors.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.Props;
import com.droidkit.actors.concurrency.FutureCallback;
import com.droidkit.mvvm.preferences.PreferenceBoolean;
import com.droidkit.mvvm.preferences.PreferenceString;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import im.actor.api.scheme.rpc.ResponseVoid;
import im.actor.api.util.ExponentialBackoff;
import im.actor.messenger.BuildConfig;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.api.SequenceActor;
import im.actor.messenger.util.Logger;

import java.io.IOException;

import static im.actor.messenger.core.Core.auth;
import static im.actor.messenger.core.Core.requests;

/**
 * Created by ex3ndr on 18.09.14.
 */
public class GooglePushActor extends Actor {

    private static final String TAG = "GooglePush";
    private static final long SENDER_ID = 209133700967L;

    public static ActorRef push() {
        return ActorSystem.system().actorOf(Props.create(GooglePushActor.class).changeDispatcher("push"), "google_push");
    }

    private SharedPreferences sharedPreferences;

    private PreferenceBoolean isRegistered;
    private PreferenceBoolean isRegisteredOnServer;
    private PreferenceString token;
    private ExponentialBackoff backoff;

    @Override
    public void preStart() {
        if (!BuildConfig.ENABLE_GOOGLE_PLAY) {
            return;
        }

        sharedPreferences = AppContext.getContext().getSharedPreferences("google_push.ini", Context.MODE_PRIVATE);
        isRegistered = new PreferenceBoolean("push.registered", sharedPreferences, false);
        isRegisteredOnServer = new PreferenceBoolean("push.registered_server", sharedPreferences, false);
        token = new PreferenceString("push.token", sharedPreferences, null);
        backoff = new ExponentialBackoff();
        // Logger.d(TAG, "Starting im.actor.messenger.core.actors.push");

        if (!isRegistered.getValue()) {
            Logger.d(TAG, "Requesting registration");
            self().send(new TryRegister());
        } else {
            Logger.d(TAG, "Already registered");
            if (!isRegisteredOnServer.getValue()) {
                Logger.d(TAG, "Not sent to server: requesting send");
                sendToken();
            }
        }
    }

    @Override
    public void onReceive(Object message) {
        if (!BuildConfig.ENABLE_GOOGLE_PLAY) {
            return;
        }

        if (message instanceof Intent) {
            Intent intent = (Intent) message;
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(AppContext.getContext());
            String messageType = gcm.getMessageType(intent);
            if (!extras.isEmpty()) {
                // Logger.d(TAG, "Received message: " + messageType);
                if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                    if (extras.containsKey("seq")) {
                        int seq = Integer.parseInt(extras.getString("seq"));
                        // Logger.d(TAG, "Received seq: " + seq);
                        if (auth().isAuthorized()) {
                            system().actorOf(SequenceActor.sequence()).send(new SequenceActor.ExternalSequence(seq));
                        } else {
                            // Logger.d(TAG, "Ignoring: not authorized");
                        }
                    } else {
                        Logger.w(TAG, "Unknown message");
                    }
                }
            } else {
                Logger.w(TAG, "Message with null extras: ignoring");
            }
        } else if (message instanceof TryRegister) {
            Logger.d(TAG, "Trying to register");
            try {
                String regId = GoogleCloudMessaging.getInstance(AppContext.getContext()).register("" + SENDER_ID);
                token.change(regId);
                isRegistered.change(true);
                Logger.d(TAG, "Token registered, sending to server");
                sendToken();
            } catch (IOException e) {
                e.printStackTrace();
                backoff.onFailure();
                long wait = backoff.exponentialWait();
                Logger.d(TAG, "Unable to register, waiting for " + wait + " ms");
                self().sendOnce(new TryRegister(), wait);
            }
        } else {
            drop(message);
        }
    }

    private void sendToken() {
        if (!BuildConfig.ENABLE_GOOGLE_PLAY) {
            return;
        }

        ask(requests().registerGooglePush(SENDER_ID, token.getValue()), new FutureCallback<ResponseVoid>() {
            @Override
            public void onResult(ResponseVoid result) {
                Logger.d(TAG, "Token sent to server");
                isRegisteredOnServer.change(true);
            }

            @Override
            public void onError(Throwable throwable) {
                Logger.d(TAG, "Unable to sent token to server: " + throwable.getMessage());
            }
        });
    }

    private class TryRegister {

    }
}