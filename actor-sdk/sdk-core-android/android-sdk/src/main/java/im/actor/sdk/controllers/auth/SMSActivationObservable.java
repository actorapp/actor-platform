package im.actor.sdk.controllers.auth;

import java.util.Observable;

/**
 * Created by 98379720172 on 25/10/16.
 */

public class SMSActivationObservable extends Observable {

    private static SMSActivationObservable instance = new SMSActivationObservable();

    public static SMSActivationObservable getInstance(){
        return  instance;
    }

    public void updateValue(Object data){
        synchronized (this){
            setChanged();
            notifyObservers(data);
        }
    }
}
