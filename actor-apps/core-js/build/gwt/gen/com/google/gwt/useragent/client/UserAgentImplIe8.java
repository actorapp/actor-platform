package com.google.gwt.useragent.client;

public class UserAgentImplIe8 implements com.google.gwt.useragent.client.UserAgent {
  
  public native String getRuntimeValue() /*-{
    var ua = navigator.userAgent.toLowerCase();
    var docMode = $doc.documentMode;
    if ((function() { 
      return (ua.indexOf('webkit') != -1);
    })()) return 'safari';
    if ((function() { 
      return (ua.indexOf('msie') != -1 && (docMode >= 10 && docMode < 11));
    })()) return 'ie10';
    if ((function() { 
      return (ua.indexOf('msie') != -1 && (docMode >= 9 && docMode < 11));
    })()) return 'ie9';
    if ((function() { 
      return (ua.indexOf('msie') != -1 && (docMode >= 8 && docMode < 11));
    })()) return 'ie8';
    if ((function() { 
      return (ua.indexOf('gecko') != -1 || docMode >= 11);
    })()) return 'gecko1_8';
    return 'unknown';
  }-*/;
  
  
  public String getCompileTimeValue() {
    return "ie8";
  }
}
