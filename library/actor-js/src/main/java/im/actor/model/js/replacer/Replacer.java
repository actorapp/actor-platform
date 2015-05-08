/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.replacer;

import im.actor.model.entity.Peer;
import im.actor.model.js.JsMessenger;
import im.actor.model.js.providers.http.JsHttpRequest;
import im.actor.model.js.providers.http.JsHttpRequestHandler;

/**
 * Tool that replaces input content on text send
 */
public class Replacer {
    private JsMessenger messenger;

    public Replacer(JsMessenger messenger) {
        this.messenger = messenger;
    }

    public boolean canHack(Peer peer, String text) {
        // Hack for dropbox screenshots
        if (text.contains("www.dropbox.com")) {
            if (!text.trim().contains(" ")) {
                hackDropBox(peer, text.trim());
                return true;
            }
        }

        return false;
    }

    private void hackDropBox(Peer peer, String text) {
        String url = text.replace("www.dropbox.com", "dl.dropboxusercontent.com");
        hackImageUrl(peer, url, text);
    }

    private void hackImageUrl(final Peer peer, String url, final String baseText) {
        JsHttpRequest request = JsHttpRequest.create();
        request.setResponseType("blob");
        request.open("GET", url);
        request.setOnLoadHandler(new JsHttpRequestHandler() {
            @Override
            public void onStateChanged(JsHttpRequest request) {
                if (request.getReadyState() == 4) {
                    if (request.getStatus() == 200) {
                        // TODO: Correct file name
                        messenger.sendPhoto(peer, "screenshot.png", request.getResponseBlob());
                    } else {
                        messenger.sendNoHack(peer, baseText);
                    }
                }
            }
        });
        request.send();
    }
}