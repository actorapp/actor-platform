/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.notification;

import com.google.gwt.core.client.JavaScriptObject;

public class JsChromePush extends JavaScriptObject {

    public static native boolean isSupported()/*-{
        if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
            console.warn('Notifications aren\'t supported.');
            return false;
        }

        if (Notification.permission === 'denied') {
            console.warn('The user has blocked notifications.');
            return false;
        }

        if (!('PushManager' in window)) {
            console.warn('Push messaging isn\'t supported.');
            return false;
        }

        return true;
    }-*/;

    public static native void subscribe(PushSubscribeResult callback)/*-{

        function endpointWorkaround(pushSubscription) {
            // Make sure we only mess with GCM
            if (pushSubscription.endpoint.indexOf('https://android.googleapis.com/gcm/send') !== 0) {
                return pushSubscription.endpoint;
            }

            var mergedEndpoint = pushSubscription.endpoint;
            // Chrome 42 + 43 will not have the subscriptionId attached
            // to the endpoint.
            if (pushSubscription.subscriptionId &&
                pushSubscription.endpoint.indexOf(pushSubscription.subscriptionId) === -1) {
                // Handle version 42 where you have separate subId and Endpoint
                mergedEndpoint = pushSubscription.endpoint + '/' + pushSubscription.subscriptionId;
            }

            return mergedEndpoint;
        }

         $wnd.navigator.serviceWorker.register('push-worker.js').then(function() {
            $wnd.navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
                serviceWorkerRegistration.pushManager.subscribe({userVisibleOnly: true})
                    .then(function(subscription) {
                        var mergedEndpoint = endpointWorkaround(subscription);

                        if (mergedEndpoint.indexOf('https://android.googleapis.com/gcm/send') !== 0) {
                            console.warn('Unsupported endpoint: ' + mergedEndpoint);
                            return;
                        }

                        var endpointSections = mergedEndpoint.split('/');
                        var subscriptionId = endpointSections[endpointSections.length - 1];

                        callback.@im.actor.model.js.providers.notification.PushSubscribeResult::onSubscribedChrome(*)(subscriptionId);
                        return;
                    });
             });
         });
    }-*/;

    public static native JsChromePush create()/*-{
        return {}
    }-*/;

    protected JsChromePush() {

    }
}
