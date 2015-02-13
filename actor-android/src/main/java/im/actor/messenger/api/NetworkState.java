package im.actor.messenger.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import im.actor.messenger.core.AppContext;

import static im.actor.messenger.core.Core.core;

public class NetworkState {

    private static volatile NetworkState instance;

    public static NetworkState getInstance() {
        if (instance == null) {
            synchronized (NetworkState.class) {
                if (instance == null) {
                    instance = new NetworkState();
                    instance.initBroadcastReceiver();
                }
            }
        }
        return instance;
    }

    private NetworkState() {
    }

    private void initBroadcastReceiver() {
        AppContext.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                core().getActorApi().notifyNetworkChanged();
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
}
