package im.actor.model.js.angular;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class AngularListCallbackExporterImpl implements Exporter, im.actor.model.js.angular.AngularListCallback {
    private com.google.gwt.core.client.JavaScriptObject jso;
    
    public boolean equals(Object obj) {
      return obj != null && obj instanceof AngularListCallbackExporterImpl && jso.equals(((AngularListCallbackExporterImpl)obj).jso);
    }
    public AngularListCallbackExporterImpl() { export(); }
    public AngularListCallbackExporterImpl(com.google.gwt.core.client.JavaScriptObject jso) {
      this.jso = jso;
    }
    
    public static AngularListCallbackExporterImpl makeClosure(com.google.gwt.core.client.JavaScriptObject closure) {
      return new AngularListCallbackExporterImpl(closure);
    }
    
    public void onCollectionChanged(com.google.gwt.core.client.JsArray a0) {
      invoke(jso ,a0);
    }
    public native void invoke(com.google.gwt.core.client.JavaScriptObject closure, com.google.gwt.core.client.JsArray a0) /*-{
      closure.apply(a0 ,[a0]);
    }-*/;
    
    public native void export0() /*-{
      var pkg = @org.timepedia.exporter.client.ExporterUtil::declarePackage(Ljava/lang/String;)('im.actor.model.js.angular.AngularListCallback');
      var _;
      $wnd.im.actor.model.js.angular.AngularListCallback = $entry(function() {
        var g, j = this;
        if (@org.timepedia.exporter.client.ExporterUtil::isAssignableToInstance(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)(@im.actor.model.js.angular.AngularListCallback::class, arguments))
          g = arguments[0];
        j.g = g;
        @org.timepedia.exporter.client.ExporterUtil::setWrapper(Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(g, j);
        return j;
      });
      _ = $wnd.im.actor.model.js.angular.AngularListCallback.prototype = new Object();
      _.onCollectionChanged = $entry(function(a0) { 
        this.g.@im.actor.model.js.angular.AngularListCallback::onCollectionChanged(Lcom/google/gwt/core/client/JsArray;)(a0);
      });
      
      @org.timepedia.exporter.client.ExporterUtil::addTypeMap(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)
       (@im.actor.model.js.angular.AngularListCallback::class, $wnd.im.actor.model.js.angular.AngularListCallback);
      
      if(pkg) for (p in pkg) if ($wnd.im.actor.model.js.angular.AngularListCallback[p] === undefined) $wnd.im.actor.model.js.angular.AngularListCallback[p] = pkg[p];
    }-*/;
    private static boolean exported;
    public void export() { 
      if(!exported) {
        exported=true;
        export0();
      }
    }
}
