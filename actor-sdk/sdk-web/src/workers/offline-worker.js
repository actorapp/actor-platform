importScripts('serviceworker-cache-polyfill.js');

const CACHE_NAME = 'actor-cache';
const REQUIRED_FILES = [
  '/',
  '/index.html',
  '/app.js',
  '/styles.js',

  '/assets/fonts/MaterialIcons-Regular.woff',
  '/assets/fonts/RobotoDraft-Bold.woff',
  '/assets/fonts/RobotoDraft-Light.woff',
  '/assets/fonts/RobotoDraft-Medium.woff',
  '/assets/fonts/RobotoDraft-Regular.woff',
  '/assets/fonts/MaterialIcons-Regular.woff2',
  '/assets/fonts/RobotoDraft-Bold.woff2',
  '/assets/fonts/RobotoDraft-Light.woff2',
  '/assets/fonts/RobotoDraft-Medium.woff2',
  '/assets/fonts/RobotoDraft-Regular.woff2',
  '/assets/fonts/MaterialIcons-Regular.eot',
  '/assets/fonts/RobotoDraft-Bold.eot',
  '/assets/fonts/RobotoDraft-Light.eot',
  '/assets/fonts/RobotoDraft-Medium.eot',
  '/assets/fonts/RobotoDraft-Regular.eot',
  '/assets/fonts/MaterialIcons-Regular.ttf',
  '/assets/fonts/RobotoDraft-Bold.ttf',
  '/assets/fonts/RobotoDraft-Light.ttf',
  '/assets/fonts/RobotoDraft-Medium.ttf',
  '/assets/fonts/RobotoDraft-Regular.ttf',

  '/assets/images/emoji/sheet_apple_64.png',
  '/assets/images/emoji/sheet_emojione_64.png',
  '/assets/images/emoji/sheet_google_64.png',
  '/assets/images/emoji/sheet_twitter_64.png',
  '/assets/images/favicon_notification.png',
  '/assets/images/favicon.png',
  '/assets/images/logo_splash.png',
  '/assets/images/logo.png',
  '/assets/images/logo@2x.png',
  '/assets/images/notification_icon_512.png',
  '/assets/images/icons.svg',

  '/assets/sound/notification.mp3'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('[SW] Install: Caches opened, adding all core components to cache');
        return cache.addAll(REQUIRED_FILES.map((REQUIRED_FILES) => new Request(REQUIRED_FILES)))
      })
      .then(() => {
        console.log('[SW] Install: All required resources have been cached');
        self.skipWaiting()
      })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        if (response) {
          console.log('[SW] Fetch: Returning from ServiceWorker cache: ', event.request.url);
          return response;
        }

        console.log('[SW] Fetch: Returning from server: ', event.request.url);
        return fetch(event.request)
      })
  );
});
