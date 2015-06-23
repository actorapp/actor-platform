package com.google.gwt.i18n.client.impl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import java.util.HashMap;

public class LocaleInfoImpl_shared extends com.google.gwt.i18n.client.impl.LocaleInfoImpl {
  private static native String getLocaleNativeDisplayName(
      JavaScriptObject nativeDisplayNamesNative,String localeName) /*-{
    return nativeDisplayNamesNative[localeName];
  }-*/;
  
  HashMap<String,String> nativeDisplayNamesJava;
  private JavaScriptObject nativeDisplayNamesNative;
  
  @Override
  public String[] getAvailableLocaleNames() {
    return new String[] {
      "default",
    };
  }
  
  @Override
  public String getLocaleNativeDisplayName(String localeName) {
    if (GWT.isScript()) {
      if (nativeDisplayNamesNative == null) {
        nativeDisplayNamesNative = loadNativeDisplayNamesNative();
      }
      return getLocaleNativeDisplayName(nativeDisplayNamesNative, localeName);
    } else {
      if (nativeDisplayNamesJava == null) {
        nativeDisplayNamesJava = new HashMap<String, String>();
      }
      return nativeDisplayNamesJava.get(localeName);
    }
  }
  
  @Override
  public boolean hasAnyRTL() {
    return false;
  }
  
  private native JavaScriptObject loadNativeDisplayNamesNative() /*-{
    return {
    };
  }-*/;
}
