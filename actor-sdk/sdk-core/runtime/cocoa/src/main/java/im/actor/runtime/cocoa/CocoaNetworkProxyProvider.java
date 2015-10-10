package im.actor.runtime.cocoa;

import com.google.j2objc.annotations.ObjectiveCName;

import im.actor.runtime.NetworkRuntime;
import im.actor.runtime.mtproto.ConnectionCallback;
import im.actor.runtime.mtproto.ConnectionEndpoint;
import im.actor.runtime.mtproto.CreateConnectionCallback;

public class CocoaNetworkProxyProvider implements NetworkRuntime {

    private static NetworkRuntime networkRuntime;

    @ObjectiveCName("setNetworkRuntime:")
    public static void setNetworkRuntime(NetworkRuntime networkRuntime) {
        CocoaNetworkProxyProvider.networkRuntime = networkRuntime;
    }

    @Override
    public void createConnection(int connectionId, int mtprotoVersion, int apiMajorVersion,
                                 int apiMinorVersion, ConnectionEndpoint endpoint, ConnectionCallback callback, CreateConnectionCallback createCallback) {
        networkRuntime.createConnection(connectionId, mtprotoVersion, apiMajorVersion, apiMinorVersion, endpoint, callback, createCallback);
    }
}
