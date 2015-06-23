package im.actor.model.js.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class JsAuthErrorClosureExporterImpl implements Exporter, im.actor.model.js.entity.JsAuthErrorClosure {
    private com.google.gwt.core.client.JavaScriptObject jso;
    
    public boolean equals(Object obj) {
      return obj != null && obj instanceof JsAuthErrorClosureExporterImpl && jso.equals(((JsAuthErrorClosureExporterImpl)obj).jso);
    }
    public JsAuthErrorClosureExporterImpl() { export(); }
    public JsAuthErrorClosureExporterImpl(com.google.gwt.core.client.JavaScriptObject jso) {
      this.jso = jso;
    }
    
    public static JsAuthErrorClosureExporterImpl makeClosure(com.google.gwt.core.client.JavaScriptObject closure) {
      return new JsAuthErrorClosureExporterImpl(closure);
    }
    
    public void onError(java.lang.String a0, java.lang.String a1, boolean a2, java.lang.String a3) {
      invoke(jso ,a0, a1, a2, a3);
    }
    public native void invoke(com.google.gwt.core.client.JavaScriptObject closure, java.lang.String a0, java.lang.String a1, boolean a2, java.lang.String a3) /*-{
      closure.apply(null ,[a0, a1, a2, a3]);
    }-*/;
    
    public native void export0() /*-{
      var pkg = @org.timepedia.exporter.client.ExporterUtil::declarePackage(Ljava/lang/String;)('im.actor.model.js.entity.JsAuthErrorClosure');
      var _;
      $wnd.im.actor.model.js.entity.JsAuthErrorClosure = $entry(function() {
        var g, j = this;
        if (@org.timepedia.exporter.client.ExporterUtil::isAssignableToInstance(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)(@im.actor.model.js.entity.JsAuthErrorClosure::class, arguments))
          g = arguments[0];
        j.g = g;
        @org.timepedia.exporter.client.ExporterUtil::setWrapper(Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(g, j);
        return j;
      });
      _ = $wnd.im.actor.model.js.entity.JsAuthErrorClosure.prototype = new Object();
      _.onError = $entry(function(a0,a1,a2,a3) { 
        this.g.@im.actor.model.js.entity.JsAuthErrorClosure::onError(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)(a0,a1,a2,a3);
      });
      
      @org.timepedia.exporter.client.ExporterUtil::addTypeMap(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)
       (@im.actor.model.js.entity.JsAuthErrorClosure::class, $wnd.im.actor.model.js.entity.JsAuthErrorClosure);
      
      if(pkg) for (p in pkg) if ($wnd.im.actor.model.js.entity.JsAuthErrorClosure[p] === undefined) $wnd.im.actor.model.js.entity.JsAuthErrorClosure[p] = pkg[p];
    }-*/;
    private static boolean exported;
    public void export() { 
      if(!exported) {
        exported=true;
        export0();
      }
    }
}
