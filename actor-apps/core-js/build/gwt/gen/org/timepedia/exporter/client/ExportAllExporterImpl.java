package org.timepedia.exporter.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class ExportAllExporterImpl implements Exporter {
  public ExportAllExporterImpl() { export(); } 
  public void export() { 
    GWT.create(im.actor.model.js.angular.AngularListCallback.class);
    GWT.create(im.actor.model.js.angular.AngularValueCallback.class);
    GWT.create(im.actor.model.js.entity.JsAuthErrorClosure.class);
    GWT.create(im.actor.model.js.entity.JsAuthSuccessClosure.class);
    GWT.create(im.actor.model.js.entity.JsClosure.class);
    GWT.create(im.actor.model.js.JsFacade.class);
  }
}
