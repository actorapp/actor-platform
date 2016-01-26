package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import im.actor.core.entity.Message;
import im.actor.core.viewmodel.ConversationVM;
import im.actor.runtime.js.mvvm.JsDisplayList;
import im.actor.runtime.js.mvvm.JsDisplayListBind;
import im.actor.runtime.js.mvvm.JsDisplayListCallback;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;

@Export
public class JsMessagesBind implements Exportable, JsDisplayListCallback<JsMessage>, ValueChangedListener<Boolean> {

    private final JsDisplayList<JsMessage, Message> displayList;
    private final JsDisplayListBind<JsMessage, Message> bind;
    private final ConversationVM conversationVM;
    private final JsMessagesBindClosure closure;
    private boolean isLoaded;
    private JsArray<JsMessage> arrays;
    private JsArray<JavaScriptObject> overlays;
    private boolean isLocked = true;

    @Export
    public JsMessagesBind(JsMessagesBindClosure closure,
                          JsDisplayList<JsMessage, Message> displayList,
                          ConversationVM conversationVM) {
        this.displayList = displayList;
        this.closure = closure;
        this.conversationVM = conversationVM;
        this.conversationVM.getIsLoaded().subscribe(this);
        this.bind = displayList.subscribe(this, true);
        this.bind.initAll();

        notifySubscriber();
        this.isLocked = false;
    }

    @Export
    public void unbind() {
        this.displayList.unsubscribe(this);
    }

    @Override
    public void onCollectionChanged(JsArray<JsMessage> array, JsArray<JavaScriptObject> overlays) {
        this.arrays = array;
        this.overlays = overlays;
        if (!isLocked) {
            notifySubscriber();
        }
    }

    @Override
    public void onChanged(Boolean val, Value<Boolean> valueModel) {
        this.isLoaded = val;
        if (!isLocked) {
            notifySubscriber();
        }
    }

    private void notifySubscriber() {
        closure.onBind(arrays, overlays, isLoaded);
    }
}
