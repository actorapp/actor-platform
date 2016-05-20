package im.actor.sdk.controllers.calls.view;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import im.actor.runtime.actors.Actor;

public class TimerActor extends Actor {
    private final int inteval;
    private HashMap<Integer, TimerCallback> callbacks = new HashMap<Integer, TimerCallback>();
    private HashMap<Integer, Long> callbacksRegisterTime = new HashMap<Integer, Long>();
    public TimerActor(int inteval) {
        this.inteval = inteval;
    }

    @Override
    public void preStart() {
        super.preStart();
        schedule(new Tick(), inteval);
    }

    @Override
    public void onReceive(Object message) {
        if(message instanceof Register){
            callbacks.put(((Register) message).getId(), ((Register) message).getCallback());
            if(!callbacksRegisterTime.keySet().contains(((Register) message).getId())){
                callbacksRegisterTime.put(((Register) message).getId(), System.currentTimeMillis());
            }
        }else if(message instanceof UnRegister){
            callbacks.remove(((UnRegister) message).getId());
            callbacksRegisterTime.remove(((UnRegister) message).getId());
        }else if(message instanceof Tick){
            onTick();
        }
    }

    private void onTick() {
        for (int callbackId:callbacks.keySet()) {
            long currentTime = System.currentTimeMillis();
            callbacks.get(callbackId).onTick(currentTime, System.currentTimeMillis() - callbacksRegisterTime.get(callbackId));
        }
        schedule(new Tick(), inteval);
    }

    public static class Register{
        TimerCallback callback;
        int id;
        public Register(TimerCallback callback, int timerId) {
            this.callback = callback;
            this.id = timerId;
        }

        public int getId() {
            return id;
        }

        public TimerCallback getCallback() {
            return callback;
        }
    }

    public static class UnRegister{
        int id;

        public UnRegister(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private static class Tick{

    }

    public interface TimerCallback{
        void onTick(long currentTime, long timeFromRegister);
    }
}
