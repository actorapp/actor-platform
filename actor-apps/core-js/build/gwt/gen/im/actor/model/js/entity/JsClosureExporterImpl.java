package im.actor.model.js.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class JsClosureExporterImpl implements Exporter, im.actor.model.js.entity.JsClosure {
    private com.google.gwt.core.client.JavaScriptObject jso;
    
    public boolean equals(Object obj) {
      return obj != null && obj instanceof JsClosureExporterImpl && jso.equals(((JsClosureExporterImpl)obj).jso);
    }
    public JsClosureExporterImpl() { export(); }
    public JsClosureExporterImpl(com.google.gwt.core.client.JavaScriptObject jso) {
      this.jso = jso;
    }
    
    public static JsClosureExporterImpl makeClosure(com.google.gwt.core.client.JavaScriptObject closure) {
      return new JsClosureExporterImpl(closure);
    }
    
    public void callback() {
      invoke(jso );
    }
    public native void invoke(com.google.gwt.core.client.JavaScriptObject closure) /*-{
      closure.apply(null ,[]);
    }-*/;
    
    public native void export0() /*-{
      var pkg = @org.timepedia.exporter.client.ExporterUtil::declarePackage(Ljava/lang/String;)('im.actor.model.js.entity.JsClosure');
      var _;
      $wnd.im.actor.model.js.entity.JsClosure = $entry(function() {
        var g, j = this;
        if (@org.timepedia.exporter.client.ExporterUtil::isAssignableToInstance(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)(@im.actor.model.js.entity.JsClosure::class, arguments))
          g = arguments[0];
        j.g = g;
        @org.timepedia.exporter.client.ExporterUtil::setWrapper(Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(g, j);
        return j;
      });
      _ = $wnd.im.actor.model.js.entity.JsClosure.prototype = new Object();
      _.callback = $entry(function() { 
        this.g.@im.actor.model.js.entity.JsClosure::callback()();
      });
      
      @org.timepedia.exporter.client.ExporterUtil::addTypeMap(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)
       (@im.actor.model.js.entity.JsClosure::class, $wnd.im.actor.model.js.entity.JsClosure);
      
      if(pkg) for (p in pkg) if ($wnd.im.actor.model.js.entity.JsClosure[p] === undefined) $wnd.im.actor.model.js.entity.JsClosure[p] = pkg[p];
    }-*/;
    private static boolean exported;
    public void export() { 
      if(!exported) {
        exported=true;
        export0();
      }
    }
}
