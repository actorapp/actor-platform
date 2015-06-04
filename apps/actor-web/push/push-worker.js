'use strict';

var isTabOpen = false;
var timerId;

self.addEventListener('message', function(event) {

  if (event.data === 'tabOpenNotify') {
    if (timerId) {
      clearTimeout(timerId);
      timerId = null;
    }
    timerId = setTimeout(function() {
      timerId = null;
      isTabOpen = false;
    }, 2000);

    isTabOpen = true;
  }
});

self.addEventListener('push', function(event) {

  if (isTabOpen) {
    return;
  }

  var title = 'New Message';
  var body = 'New Message in Actor';
  var icon = 'assets/img/notification_icon_512.png';
  var tag = 'new-message';

  event.waitUntil(
    self.registration.showNotification(title, {
      body: body,
      icon: icon,
      tag: tag
    })
  );
});

self.addEventListener('notificationclick', function(event) {

  // Android doesn't close the notification when you click on it
  // See: http://crbug.com/463146
  event.notification.close();

  // This looks to see if the current is already open and
  // focuses if it is
  event.waitUntil(
    clients.matchAll({
      type: "window"
    })
    .then(function(clientList) {
      for (var i = 0; i < clientList.length; i++) {
        var client = clientList[i];
        if (client.url == '/' && 'focus' in client)
          return client.focus();
      }
      if (clients.openWindow) {
        return clients.openWindow('/');
      }
    })
  );
});
