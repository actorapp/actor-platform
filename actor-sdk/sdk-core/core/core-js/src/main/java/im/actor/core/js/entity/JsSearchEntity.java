package im.actor.core.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.core.entity.SearchEntity;
import im.actor.core.js.JsMessenger;
import im.actor.runtime.js.mvvm.JsEntityConverter;

public class JsSearchEntity extends JavaScriptObject {

    public static final JsEntityConverter<SearchEntity, JsSearchEntity> CONVERTER = new JsEntityConverter<SearchEntity, JsSearchEntity>() {

        @Override
        public JsSearchEntity convert(SearchEntity value) {
            JsMessenger messenger = JsMessenger.getInstance();
            return JsSearchEntity.create(messenger.buildPeerInfo(value.getPeer()));
        }

        @Override
        public boolean isSupportOverlays() {
            return false;
        }

        @Override
        public JavaScriptObject buildOverlay(SearchEntity prev, SearchEntity current, SearchEntity next) {
            return null;
        }
    };

    public static native JsSearchEntity create(JsPeerInfo peerInfo)/*-{
        return {peerInfo: peerInfo};
    }-*/;

    protected JsSearchEntity() {

    }
}