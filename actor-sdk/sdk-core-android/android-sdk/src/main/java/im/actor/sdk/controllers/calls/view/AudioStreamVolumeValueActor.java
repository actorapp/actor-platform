package im.actor.sdk.controllers.calls.view;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.ArrayList;
import java.util.Arrays;

import im.actor.runtime.Log;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.messages.PoisonPill;
import im.actor.sdk.core.audio.OpusEncoderActor;
import im.actor.sdk.core.audio.VoiceBuffers;

public class AudioStreamVolumeValueActor extends Actor {
    private static final int BUFFER_SIZE = 16 * 1024;
    private int delay;
    private AudioRecord audioRecord;
    boolean inited = false;

    public AudioStreamVolumeValueActor() {
    }

    ArrayList<VolumeValueListener> callbacks = new ArrayList<VolumeValueListener>();

    @Override
    public void preStart() {
        delay = 10;
    }

    private void subscribeForVolume(VolumeValueListener callback){
        if(audioRecord == null){
            int minBufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int bufferSize = 16 * minBufferSize;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioRecord.startRecording();
        }
        callbacks.add(callback);
        if(!inited){
            inited = true;
            schedule(new Check(), delay);
        }
    }

    private void unsubscribeFromVolume(VolumeValueListener callback){
        callbacks.remove(callback);
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        self().send(PoisonPill.INSTANCE);
    }

    private void check(){

        if(audioRecord!=null){
            byte[] buffer = VoiceBuffers.getInstance().obtainBuffer(BUFFER_SIZE);
            int len = audioRecord.read(buffer, 0, buffer.length);
            if (len > 0) {
                int i = getMax(buffer) - getMin(buffer);
                for (VolumeValueListener c:callbacks) {
                    c.onVolumeValue(i);
                }
            } else {
                VoiceBuffers.getInstance().releaseBuffer(buffer);
            }

        }


        self().send(new Check());
    }

    byte getMax(byte[] array){
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    byte getMin(byte[] array){
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }


    @Override
    public void onReceive(Object message) {
        if(message instanceof Check){
            check();
        }else if(message instanceof Subscribe){
            subscribeForVolume(((Subscribe) message).getCallback());
        }else if(message instanceof Unsubscribe){
            unsubscribeFromVolume(((Unsubscribe) message).getCallback());
        }
    }

    interface VolumeValueListener{
        void onVolumeValue(int val);
    }
    
    public static class Subscribe{
        VolumeValueListener callback;

        public Subscribe(VolumeValueListener callback) {
            this.callback = callback;
        }

        public VolumeValueListener getCallback() {
            return callback;
        }
    }

    public static class Unsubscribe{
        VolumeValueListener callback;

        public Unsubscribe(VolumeValueListener callback) {
            this.callback = callback;
        }

        public VolumeValueListener getCallback() {
            return callback;
        }
    }
    
    private static class Check{}
}
