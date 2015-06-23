package im.actor.model.js.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class JsAuthSuccessClosureExporterImpl implements Exporter, im.actor.model.js.entity.JsAuthSuccessClosure {
    private com.google.gwt.core.client.JavaScriptObject jso;
    
    public boolean equals(Object obj) {
      return obj != null && obj instanceof JsAuthSuccessClosureExporterImpl && jso.equals(((JsAuthSuccessClosureExporterImpl)obj).jso);
    }
    public JsAuthSuccessClosureExporterImpl() { export(); }
    public JsAuthSuccessClosureExporterImpl(com.google.gwt.core.client.JavaScriptObject jso) {
      this.jso = jso;
    }
    
    public static JsAuthSuccessClosureExporterImpl makeClosure(com.google.gwt.core.client.JavaScriptObject closure) {
      return new JsAuthSuccessClosureExporterImpl(closure);
    }
    
    public void onResult(java.lang.String a0) {
      invoke(jso ,a0);
    }
    public native void invoke(com.google.gwt.core.client.JavaScriptObject closure, java.lang.String a0) /*-{
      closure.apply(null ,[a0]);
    }-*/;
    
    public native void export0() /*-{
      var pkg = @org.timepedia.exporter.client.ExporterUtil::declarePackage(Ljava/lang/String;)('im.actor.model.js.entity.JsAuthSuccessClosure');
      var _;
      $wnd.im.actor.model.js.entity.JsAuthSuccessClosure = $entry(function() {
        var g, j = this;
        if (@org.timepedia.exporter.client.ExporterUtil::isAssignableToInstance(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)(@im.actor.model.js.entity.JsAuthSuccessClosure::class, arguments))
          g = arguments[0];
        j.g = g;
        @org.timepedia.exporter.client.ExporterUtil::setWrapper(Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(g, j);
        return j;
      });
      _ = $wnd.im.actor.model.js.entity.JsAuthSuccessClosure.prototype = new Object();
      _.onResult = $entry(function(a0) { 
        this.g.@im.actor.model.js.entity.JsAuthSuccessClosure::onResult(Ljava/lang/String;)(a0);
      });
      
      @org.timepedia.exporter.client.ExporterUtil::addTypeMap(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)
       (@im.actor.model.js.entity.JsAuthSuccessClosure::class, $wnd.im.actor.model.js.entity.JsAuthSuccessClosure);
      
      if(pkg) for (p in pkg) if ($wnd.im.actor.model.js.entity.JsAuthSuccessClosure[p] === undefined) $wnd.im.actor.model.js.entity.JsAuthSuccessClosure[p] = pkg[p];
    }-*/;
    private static boolean exported;
    public void export() { 
      if(!exported) {
        exported=true;
        export0();
      }
    }
}
