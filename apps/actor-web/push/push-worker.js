'use strict';

self.addEventListener('push', function(event) {
  console.log('Received a push message', event);

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
