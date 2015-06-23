package com.google.gwt.user.client;

import com.google.gwt.user.client.DocumentModeAsserter.Severity;

public class DocumentModeAsserter_DocumentModeProperty implements com.google.gwt.user.client.DocumentModeAsserter.DocumentModeProperty {
  
  public String[] getAllowedDocumentModes() {
    return new String[] {
      "CSS1Compat", 
    };
  }
  
  public Severity getDocumentModeSeverity() {
    return Severity.WARN;
  }
  
}
