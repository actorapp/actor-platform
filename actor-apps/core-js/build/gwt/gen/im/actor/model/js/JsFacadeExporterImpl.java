package im.actor.model.js;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.timepedia.exporter.client.Exporter;
import org.timepedia.exporter.client.ExporterUtil;

public class JsFacadeExporterImpl implements Exporter {
    public static im.actor.model.js.entity.JsPromise __static_wrapper_createGroup(im.actor.model.js.JsFacade instance, java.lang.String a0, im.actor.model.js.providers.fs.JsFile a1, JavaScriptObject a2) {
      return instance.createGroup(a0, a1, ExporterUtil.toArrInt(a2));
    }
    public JsFacadeExporterImpl() { export(); }
    public native void export0() /*-{
      var pkg = @org.timepedia.exporter.client.ExporterUtil::declarePackage(Ljava/lang/String;)('actor.ActorApp');
      var _;
      $wnd.actor.ActorApp = $entry(function() {
        var g, j = this;
        if (@org.timepedia.exporter.client.ExporterUtil::isAssignableToInstance(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)(@im.actor.model.js.JsFacade::class, arguments))
          g = arguments[0];
        else if (arguments.length == 0)
          g = @im.actor.model.js.JsFacadeExporterImpl::___create()();
        else if (arguments.length == 1)
          g = @im.actor.model.js.JsFacadeExporterImpl::___create([Ljava/lang/String;)(arguments[0]);
        j.g = g;
        @org.timepedia.exporter.client.ExporterUtil::setWrapper(Ljava/lang/Object;Lcom/google/gwt/core/client/JavaScriptObject;)(g, j);
        return j;
      });
      _ = $wnd.actor.ActorApp.prototype = new Object();
      _.addContact = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::addContact(I)(a0);
      });
      _.bindChat = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::bindChat(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/angular/AngularListCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a1.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.bindContacts = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::bindContacts(Lim/actor/model/js/angular/AngularListCallback;)(a0 == null ? null : (a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a0.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a0)));
      });
      _.bindDialogs = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::bindDialogs(Lim/actor/model/js/angular/AngularListCallback;)(a0 == null ? null : (a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a0.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a0)));
      });
      _.bindGroup = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::bindGroup(ILim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.bindTyping = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::bindTyping(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.bindUser = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::bindUser(ILim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.changeNotificationsEnabled = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::changeNotificationsEnabled(Lim/actor/model/js/entity/JsPeer;Z)(a0,a1);
      });
      _.clearChat = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::clearChat(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/entity/JsClosure;Lim/actor/model/js/entity/JsClosure;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.entity.JsClosure ? a1.g : @im.actor.model.js.entity.JsClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)),a2 == null ? null : (a2.constructor == $wnd.im.actor.model.js.entity.JsClosure ? a2.g : @im.actor.model.js.entity.JsClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a2)));
      });
      _.createGroup = $entry(function(a0,a1,a2) { 
        return @im.actor.model.js.JsFacadeExporterImpl::__static_wrapper_createGroup(Lim/actor/model/js/JsFacade;Ljava/lang/String;Lim/actor/model/js/providers/fs/JsFile;Lcom/google/gwt/core/client/JavaScriptObject;)(this.g, a0,a1,a2);
      });
      _.deleteChat = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::deleteChat(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/entity/JsClosure;Lim/actor/model/js/entity/JsClosure;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.entity.JsClosure ? a1.g : @im.actor.model.js.entity.JsClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)),a2 == null ? null : (a2.constructor == $wnd.im.actor.model.js.entity.JsClosure ? a2.g : @im.actor.model.js.entity.JsClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a2)));
      });
      $wnd.actor.ActorApp.dev1 = $entry(function() { 
        return @org.timepedia.exporter.client.ExporterUtil::wrap(Ljava/lang/Object;)(@im.actor.model.js.JsFacade::dev1()());
      });
      _.editGroupTitle = $entry(function(a0,a1) { 
        return this.g.@im.actor.model.js.JsFacade::editGroupTitle(ILjava/lang/String;)(a0,a1);
      });
      _.editMyName = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::editMyName(Ljava/lang/String;)(a0);
      });
      _.editName = $entry(function(a0,a1) { 
        return this.g.@im.actor.model.js.JsFacade::editName(ILjava/lang/String;)(a0,a1);
      });
      _.getAuthPhone = $entry(function() { 
        return this.g.@im.actor.model.js.JsFacade::getAuthPhone()();
      });
      _.getAuthState = $entry(function() { 
        return this.g.@im.actor.model.js.JsFacade::getAuthState()();
      });
      _.getGroup = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getGroup(I)(a0);
      });
      _.getGroupPeer = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getGroupPeer(I)(a0);
      });
      _.getIntegrationToken = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getIntegrationToken(I)(a0);
      });
      _.getInviteLink = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getInviteLink(I)(a0);
      });
      _.getTyping = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getTyping(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.getUid = $entry(function() { 
        return this.g.@im.actor.model.js.JsFacade::getUid()();
      });
      _.getUser = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getUser(I)(a0);
      });
      _.getUserPeer = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::getUserPeer(I)(a0);
      });
      _.inviteMember = $entry(function(a0,a1) { 
        return this.g.@im.actor.model.js.JsFacade::inviteMember(II)(a0,a1);
      });
      _.isLoggedIn = $entry(function() { 
        return this.g.@im.actor.model.js.JsFacade::isLoggedIn()();
      });
      _.isNotificationsEnabled = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::isNotificationsEnabled(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.joinGroupViaLink = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::joinGroupViaLink(Ljava/lang/String;)(a0);
      });
      _.kickMember = $entry(function(a0,a1) { 
        return this.g.@im.actor.model.js.JsFacade::kickMember(II)(a0,a1);
      });
      _.leaveGroup = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::leaveGroup(I)(a0);
      });
      _.loadDraft = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::loadDraft(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.onAppHidden = $entry(function() { 
        this.g.@im.actor.model.js.JsFacade::onAppHidden()();
      });
      _.onAppVisible = $entry(function() { 
        this.g.@im.actor.model.js.JsFacade::onAppVisible()();
      });
      _.onChatEnd = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onChatEnd(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.onConversationClosed = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onConversationClosed(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.onConversationOpen = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onConversationOpen(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      _.onDialogsClosed = $entry(function() { 
        this.g.@im.actor.model.js.JsFacade::onDialogsClosed()();
      });
      _.onDialogsEnd = $entry(function() { 
        this.g.@im.actor.model.js.JsFacade::onDialogsEnd()();
      });
      _.onDialogsOpen = $entry(function() { 
        this.g.@im.actor.model.js.JsFacade::onDialogsOpen()();
      });
      _.onMessageShown = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::onMessageShown(Lim/actor/model/js/entity/JsPeer;Ljava/lang/String;Z)(a0,a1,a2);
      });
      _.onProfileClosed = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onProfileClosed(I)(a0);
      });
      _.onProfileOpen = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onProfileOpen(I)(a0);
      });
      _.onTyping = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::onTyping(Lim/actor/model/js/entity/JsPeer;)(a0);
      });
      $wnd.actor.ActorApp.production = $entry(function() { 
        return @org.timepedia.exporter.client.ExporterUtil::wrap(Ljava/lang/Object;)(@im.actor.model.js.JsFacade::production()());
      });
      _.removeContact = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::removeContact(I)(a0);
      });
      _.requestSms = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::requestSms(Ljava/lang/String;Lim/actor/model/js/entity/JsAuthSuccessClosure;Lim/actor/model/js/entity/JsAuthErrorClosure;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.entity.JsAuthSuccessClosure ? a1.g : @im.actor.model.js.entity.JsAuthSuccessClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)),a2 == null ? null : (a2.constructor == $wnd.im.actor.model.js.entity.JsAuthErrorClosure ? a2.g : @im.actor.model.js.entity.JsAuthErrorClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a2)));
      });
      _.revokeIntegrationToken = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::revokeIntegrationToken(I)(a0);
      });
      _.revokeInviteLink = $entry(function(a0) { 
        return this.g.@im.actor.model.js.JsFacade::revokeInviteLink(I)(a0);
      });
      _.saveDraft = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::saveDraft(Lim/actor/model/js/entity/JsPeer;Ljava/lang/String;)(a0,a1);
      });
      _.sendClipboardPhoto = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::sendClipboardPhoto(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/providers/fs/JsBlob;)(a0,a1);
      });
      _.sendCode = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::sendCode(Ljava/lang/String;Lim/actor/model/js/entity/JsAuthSuccessClosure;Lim/actor/model/js/entity/JsAuthErrorClosure;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.entity.JsAuthSuccessClosure ? a1.g : @im.actor.model.js.entity.JsAuthSuccessClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)),a2 == null ? null : (a2.constructor == $wnd.im.actor.model.js.entity.JsAuthErrorClosure ? a2.g : @im.actor.model.js.entity.JsAuthErrorClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a2)));
      });
      _.sendFile = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::sendFile(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/providers/fs/JsFile;)(a0,a1);
      });
      _.sendMessage = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::sendMessage(Lim/actor/model/js/entity/JsPeer;Ljava/lang/String;)(a0,a1);
      });
      _.sendPhoto = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::sendPhoto(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/providers/fs/JsFile;)(a0,a1);
      });
      _.signUp = $entry(function(a0,a1,a2) { 
        this.g.@im.actor.model.js.JsFacade::signUp(Ljava/lang/String;Lim/actor/model/js/entity/JsAuthSuccessClosure;Lim/actor/model/js/entity/JsAuthErrorClosure;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.entity.JsAuthSuccessClosure ? a1.g : @im.actor.model.js.entity.JsAuthSuccessClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)),a2 == null ? null : (a2.constructor == $wnd.im.actor.model.js.entity.JsAuthErrorClosure ? a2.g : @im.actor.model.js.entity.JsAuthErrorClosureExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a2)));
      });
      _.unbindChat = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::unbindChat(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/angular/AngularListCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a1.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.unbindContacts = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::unbindContacts(Lim/actor/model/js/angular/AngularListCallback;)(a0 == null ? null : (a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a0.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a0)));
      });
      _.unbindDialogs = $entry(function(a0) { 
        this.g.@im.actor.model.js.JsFacade::unbindDialogs(Lim/actor/model/js/angular/AngularListCallback;)(a0 == null ? null : (a0.constructor == $wnd.im.actor.model.js.angular.AngularListCallback ? a0.g : @im.actor.model.js.angular.AngularListCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a0)));
      });
      _.unbindGroup = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::unbindGroup(ILim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.unbindTyping = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::unbindTyping(Lim/actor/model/js/entity/JsPeer;Lim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      _.unbindUser = $entry(function(a0,a1) { 
        this.g.@im.actor.model.js.JsFacade::unbindUser(ILim/actor/model/js/angular/AngularValueCallback;)(a0,a1 == null ? null : (a1.constructor == $wnd.im.actor.model.js.angular.AngularValueCallback ? a1.g : @im.actor.model.js.angular.AngularValueCallbackExporterImpl::makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(a1)));
      });
      
      @org.timepedia.exporter.client.ExporterUtil::addTypeMap(Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;)
       (@im.actor.model.js.JsFacade::class, $wnd.actor.ActorApp);
      
      if(pkg) for (p in pkg) if ($wnd.actor.ActorApp[p] === undefined) $wnd.actor.ActorApp[p] = pkg[p];
    }-*/;
    public static im.actor.model.js.JsFacade ___create() {
      return new im.actor.model.js.JsFacade();
    }
    public static im.actor.model.js.JsFacade ___create(java.lang.String[] a0) {
      return new im.actor.model.js.JsFacade(a0);
    }
    private static boolean exported;
    public void export() { 
      if(!exported) {
        exported=true;
        export0();
      }
    }
}
