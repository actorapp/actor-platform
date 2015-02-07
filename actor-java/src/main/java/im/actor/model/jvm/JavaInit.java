package im.actor.model.jvm;

import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.ThreadPriority;
import com.droidkit.actors.conf.DispatcherFactory;
import com.droidkit.actors.conf.EnvConfig;
import com.droidkit.actors.mailbox.ActorDispatcher;
import im.actor.model.config.ConnectionFactory;
import im.actor.model.jvm.actors.JavaDispatcher;
import im.actor.model.jvm.network.TcpConnection;
import im.actor.model.jvm.utils.JavaLog;
import im.actor.model.log.Log;
import im.actor.model.network.Connection;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;

import java.io.IOException;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class JavaInit {
    public static void init() {

        JavaThreads.init();

        // Init Logs
        Log.setLog(new JavaLog());

        // Setting connection factory
        ConnectionFactory.setFactory(new ConnectionFactory.Factory() {
            @Override
            public void createConnection(final int connectionId, final ConnectionEndpoint endpoint,
                                         final ConnectionCallback callback,
                                         final ConnectionFactory.CreateConnectionCallback createCallback) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            createCallback.onConnectionCreated(new TcpConnection(connectionId, endpoint, callback));
                        } catch (Exception e) {
                            e.printStackTrace();
                            createCallback.onConnectionCreateError();
                        }
                    }
                }.start();
            }
        });
    }
}
