package im.actor.model.jvm;

import im.actor.model.api.rpc.RequestSendAuthCode;
import im.actor.model.api.rpc.ResponseSendAuthCode;
import im.actor.model.jvm.network.MemoryAuthIdStorage;
import im.actor.model.log.Log;
import im.actor.model.network.*;
import im.actor.model.network.mtp.entity.Ping;
import org.junit.Test;

import java.util.Random;

public class JavaInitTest {

    private static final String TAG = "jUnit";

    private Endpoints endpoints = new Endpoints(new ConnectionEndpoint[]{
            new ConnectionEndpoint("mtproto-api.actor.im", 8080, ConnectionEndpoint.Type.TCP)});

    private Random random = new Random();

    @Test
    public void testInit() throws Exception {
        JavaInit.init();

        ActorApi actorApi = new ActorApi(endpoints, new MemoryAuthIdStorage(), new ActorApiCallback() {
            @Override
            public void onAuthIdInvalidated(long authKey) {

            }

            @Override
            public void onNewSessionCreated() {

            }

            @Override
            public void onUpdateReceived(Object obj) {

            }
        });

        actorApi.request(new RequestSendAuthCode(75552232323L, 1, "??"), new RpcCallback<ResponseSendAuthCode>() {
            @Override
            public void onResult(ResponseSendAuthCode response) {
                Log.d(TAG, "SmsHash = " + response.getSmsHash() + ", isRegistered = " + response.isRegistered());
            }

            @Override
            public void onError(RpcException e) {

            }
        });

//        AuthIdRetriever.requestAuthId(endpoints, new AuthIdRetriever.AuthIdCallback() {
//            @Override
//            public void onSuccess(long authId) {
//                MTProto mtProto = new MTProto(authId, random.nextLong(), endpoints);
//                mtProto.sendRpcMessage(new Ping(random.nextLong()));
//            }
//
//            @Override
//            public void onFailure() {
//
//            }
//        });

        Thread.sleep(30000);
    }
}