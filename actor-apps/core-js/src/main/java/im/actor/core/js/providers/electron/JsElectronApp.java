package im.actor.core.js.providers.electron;

public class JsElectronApp {

    public static native boolean isElectron()/*-{
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

    public static native void updateBadge(int count)/*-{
        var ipc = $wnd.require('ipc');
        ipc.send('tray-badge', { count: count });
    }-*/;
}
