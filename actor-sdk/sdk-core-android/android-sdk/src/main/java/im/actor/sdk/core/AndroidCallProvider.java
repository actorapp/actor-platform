package im.actor.sdk.core;

import android.content.Context;
import android.content.Intent;

import im.actor.core.providers.CallsProvider;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.calls.view.AudioActorEx;
import im.actor.sdk.controllers.calls.CallActivity;
import im.actor.sdk.core.audio.AndroidPlayerActor;
import im.actor.sdk.core.audio.AudioPlayerActor;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AndroidCallProvider implements CallsProvider {

    private ActorRef toneActor;

    @Override
    public void onCallStart(long callId) {
        Context context = ActorSDK.sharedActor().getMessenger().getContext();
        Intent callIntent = new Intent(context, CallActivity.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        callIntent.putExtra("callId", callId);
        context.startActivity(callIntent);
        context.startActivity(callIntent);
    }

    @Override
    public void onCallAnswered(long callId) {
        // TODO: Implement
    }

    @Override
    public void onCallEnd(long callId) {

    }

    @Override
    public void startOutgoingBeep() {
        if (toneActor == null) {
            toneActor = ActorSystem.system().actorOf("actor/android_tone", () ->
                    new AudioActorEx(messenger().getContext(), new AudioPlayerActor.AudioPlayerCallback() {
                        @Override
                        public void onStart(String fileName) {

                        }

                        @Override
                        public void onStop(String fileName) {

                        }

                        @Override
                        public void onPause(String fileName, float progress) {

                        }

                        @Override
                        public void onProgress(String fileName, float progress) {

                        }

                        @Override
                        public void onError(String fileName) {

                        }
                    }));
        }

        toneActor.send(new AndroidPlayerActor.Play(""));
    }

    @Override
    public void stopOutgoingBeep() {
        if (toneActor != null) {
            toneActor.send(new AudioActorEx.Stop());
        }
    }
}
