package im.actor.sdk.activation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.controllers.auth.SMSActivationObservable;

/**
 * Created by diego on 28/01/17.
 */

public class SMSActivationReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;

            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++) {
                        String format = bundle.getString("format");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        }else{
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }

                        String msgFrom = msgs[i].getOriginatingAddress();
                        String msgStr =  msgs[i].getMessageBody();

                        String arrAppName[] = msgStr.split(":");
                        String arr[] = msgStr.split("\\s+");

                        if(arrAppName[0].equalsIgnoreCase(ActorSDK.sharedActor().getAppName())){
                            Integer codigoAtivacao = Integer.parseInt(arr[ActorSDK.sharedActor().getSmsCodePosition()]);
                            SMSActivationObservable.getInstance().updateValue(codigoAtivacao.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.d("Erro ao receber SMS", e.getMessage());
                }
            }
        }
    }


}