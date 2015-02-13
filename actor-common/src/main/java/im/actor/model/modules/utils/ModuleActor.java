package im.actor.model.modules.utils;

import com.droidkit.actors.Actor;
import im.actor.model.Messenger;
import im.actor.model.network.RpcCallback;
import im.actor.model.network.RpcException;
import im.actor.model.network.parser.Request;
import im.actor.model.network.parser.Response;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class ModuleActor extends Actor {
    private Messenger messenger;

    public ModuleActor(Messenger messenger) {
        this.messenger = messenger;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public <T extends Response> void request(Request<T> request, final RpcCallback<T> callback) {
        messenger.getActorApi().request(request, new RpcCallback<T>() {
            @Override
            public void onResult(final T response) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(response);
                    }
                });
            }

            @Override
            public void onError(final RpcException e) {
                self().send(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }
        });
    }
}
