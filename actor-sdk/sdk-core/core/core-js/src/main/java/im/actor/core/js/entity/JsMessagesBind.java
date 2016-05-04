package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import im.actor.core.entity.Message;
import im.actor.core.js.annotations.UsedByApp;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.js.mvvm.JsDisplayListBind;
import im.actor.runtime.js.mvvm.JsDisplayListCallback;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;

@Export
public class JsMessagesBind implements Exportable, JsDisplayListCallback<JsMessage> {

    private final JsDisplayList<JsMessage, Message> displayList;
    private final JsDisplayListBind<JsMessage, Message> bind;
    private final ConversationVM conversationVM;
    private JsMessagesBindClosure closure;
    private JsArray<JsMessage> arrays;
    private JsArray<JavaScriptObject> overlays;
    private boolean isLocked = true;
    private boolean isInited = false;

    private ValueChangedListener<Boolean> isLoadedListener = (val, valueModel) -> {
        if (!isLocked) {
            notifySubscriber();
        }
    };
    private ValueChangedListener<Long> readDateListener = (val, valueModel) -> {
        if (!isLocked) {
            notifySubscriber();
        }
    };
    private ValueChangedListener<Long> receiveDateListener = (val, valueModel) -> {
        if (!isLocked) {
            notifySubscriber();
        }
    };

    @Export
    public JsMessagesBind(JsMessagesBindClosure closure,
                          JsDisplayList<JsMessage, Message> displayList,
                          ConversationVM conversationVM) {

        this.displayList = displayList;
        this.closure = closure;
        this.conversationVM = conversationVM;
        this.conversationVM.getIsLoaded().subscribe(isLoadedListener);
        this.conversationVM.getReadDate().subscribe(readDateListener);
        this.conversationVM.getReceiveDate().subscribe(receiveDateListener);
        this.bind = displayList.subscribe(this, true);
    }

    @Export
    public void initAll() {
        if (isInited) {
            throw new RuntimeException("Already inited!");
        }
        isInited = true;
        this.bind.initAll();
        notifySubscriber();
        this.isLocked = false;
    }

    @Override
    public void onCollectionChanged(JsArray<JsMessage> array, JsArray<JavaScriptObject> overlays) {
        this.arrays = array;
        this.overlays = overlays;
        if (!isLocked) {
            notifySubscriber();
        }
    }

    private void notifySubscriber() {
        boolean isLoaded = this.conversationVM.getIsLoaded().get();
        double readDate = this.conversationVM.getReadDate().get() / 1000.0;
        double receiveDate = this.conversationVM.getReceiveDate().get() / 1000.0;
        double readByMeDate = this.conversationVM.getOwnReadDate().get() / 1000.0;
        closure.onBind(arrays, overlays, isLoaded, receiveDate, readDate, readByMeDate);
    }

    @Export
    @UsedByApp
    public void unbind() {
        this.closure = null;
        this.displayList.unsubscribe(this);
        this.conversationVM.getIsLoaded().unsubscribe(isLoadedListener);
        this.conversationVM.getReadDate().unsubscribe(readDateListener);
        this.conversationVM.getReceiveDate().unsubscribe(receiveDateListener);
    }
}
