package im.actor.core.js.providers.electron;

/**
 * Created by ex3ndr on 31.07.15.
 */
public class JsElectronApp {
    public static native boolean isSupported()/*-{
        return 'require' in $wnd;
    }-*/;

    public static native void bounce()/*-{
        var ipc = $wnd.require('ipc');
        ipc.send('tray-bounce');
    }-*/;

    public static native void showNewMessages()/*-{
        var ipc = $wnd.require('ipc');
        ipc.send('new-messages-show');
    }-*/;

    public static native void hideNewMessages()/*-{
        var ipc = $wnd.require('ipc');
        ipc.send('new-messages-hide');
    }-*/;
}
