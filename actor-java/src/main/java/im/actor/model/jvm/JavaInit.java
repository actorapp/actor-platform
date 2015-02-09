package im.actor.model.jvm;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorRef;
import com.droidkit.actors.ActorSystem;
import com.droidkit.actors.debug.TraceInterface;
import com.droidkit.actors.mailbox.Envelope;
import im.actor.model.network.ConnectionFactory;
import im.actor.model.jvm.network.TcpConnection;
import im.actor.model.jvm.utils.JavaLog;
import im.actor.model.log.Log;
import im.actor.model.network.ConnectionCallback;
import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 06.02.15.
 */
public class JavaInit {
    public static void init() {

        JavaThreads.init();

        // Init Logs
        Log.setLog(new JavaLog());

        ActorSystem.system().setTraceInterface(new TraceInterface() {
            @Override
            public void onEnvelopeDelivered(Envelope envelope) {

            }

            @Override
            public void onEnvelopeProcessed(Envelope envelope, long duration) {

            }

            @Override
            public void onDrop(ActorRef sender, Object message, Actor actor) {
                Log.w("ACTORS", "Drop " + message);
            }

            @Override
            public void onDeadLetter(ActorRef receiver, Object message) {
                Log.w("ACTORS", "DeadLetter: " + message);
            }

            @Override
            public void onActorDie(ActorRef ref, Exception e) {
                e.printStackTrace();
            }
        });

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
