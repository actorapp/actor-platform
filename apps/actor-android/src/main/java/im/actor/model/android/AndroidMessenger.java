package im.actor.model.android;

import android.content.Context;

import im.actor.android.AndroidBaseMessenger;
import im.actor.model.Configuration;
import im.actor.model.android.modules.PushModule;

/**
 * Created by ex3ndr on 02.04.15.
 */
public class AndroidMessenger extends AndroidBaseMessenger {

    private PushModule pushModule;
    // private CallModule callModule;

    public AndroidMessenger(Context context, Configuration configuration) {
        super(context, configuration);

        pushModule = new PushModule(context, modules);
        // callModule = new CallModule(modules);

//        if (isLoggedIn()) {
//            callModule.onLoggedIn();
//        }
    }

//    @Override
//    public void onLoggedIn() {
//        super.onLoggedIn();
//
//        if (callModule != null) {
//            callModule.onLoggedIn();
//        }
//    }
//
//    public long startCall(int uid) {
//        return callModule.startCall(uid);
//    }
//
//    public void answerCall(long rid) {
//        callModule.answerCall(rid);
//    }
//
//    public void endCall(long rid) {
//        callModule.endCall(rid);
//    }
//
//    public ValueModel<CurrentCall> getCurrentCall() {
//        return callModule.getCurrentCall();
//    }
}
