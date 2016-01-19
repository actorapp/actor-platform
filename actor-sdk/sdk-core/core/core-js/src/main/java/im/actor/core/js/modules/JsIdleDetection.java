package im.actor.core.js.modules;

public class JsIdleDetection {

    public static native void subscribe(JsIdleCallback callback)/*-{
        function activeMethod() {
            callback.@im.actor.core.js.modules.JsIdleCallback::onActionDetected(*)();
        }
        $wnd.addEventListener('click', activeMethod)
        $wnd.addEventListener('mousemove', activeMethod)
        $wnd.addEventListener('mouseenter', activeMethod)
        $wnd.addEventListener('keydown', activeMethod)
        $wnd.addEventListener('scroll', activeMethod)
        $wnd.addEventListener('mousewheel', activeMethod)
        $wnd.addEventListener('touchmove', activeMethod)
        $wnd.addEventListener('touchstart', activeMethod)
    }-*/;
}
