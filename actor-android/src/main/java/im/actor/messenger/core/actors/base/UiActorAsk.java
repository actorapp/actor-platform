package im.actor.messenger.core.actors.base;

import android.os.Handler;
import android.os.Looper;

import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.concurrency.Future;
import com.droidkit.actors.concurrency.FutureCallback;

import im.actor.messenger.android.CallBarrier;

/**
 * Created by ex3ndr on 08.10.14.
 */
public class UiActorAsk {

    private static Handler handler = new Handler(Looper.getMainLooper());

    private CallBarrier callBarrier = new CallBarrier();

    public void resume() {
        callBarrier.resume();
    }

    public void pause() {
        callBarrier.pause();
    }

    public <T> void ask(Future<T> future, final UiAskCallback<T> callback) {
        callback.onPreStart();
        future.addListener(new FutureCallback<T>() {
            @Override
            public void onResult(final T t) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBarrier.call(new Runnable() {
                            @Override
                            public void run() {
                                callback.onCompleted(t);
                            }
                        });

                    }
                });
            }

            @Override
            public void onError(final Throwable throwable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(throwable);
                    }
                });
            }
        });
    }
}
