package im.actor.model.jvm;

import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.Endpoints;
import im.actor.model.network.mtp.AuthIdRetriever;
import im.actor.model.network.mtp.MTProto;
import im.actor.model.network.mtp.entity.Ping;
import org.junit.Test;

import java.util.Random;

public class JavaInitTest {

    private Endpoints endpoints = new Endpoints(new ConnectionEndpoint[]{
            new ConnectionEndpoint("mtproto-api.actor.im", 8080, ConnectionEndpoint.Type.TCP)});

    private Random random = new Random();

    @Test
    public void testInit() throws Exception {
        JavaInit.init();
        
        AuthIdRetriever.requestAuthId(endpoints, new AuthIdRetriever.AuthIdCallback() {
            @Override
            public void onSuccess(long authId) {
                MTProto mtProto = new MTProto(authId, random.nextLong(), endpoints);
                mtProto.sendMTMessage(new Ping(random.nextLong()));
            }

            @Override
            public void onFailure() {

            }
        });

        Thread.sleep(30000);
    }
}