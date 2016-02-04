if (process.env.NODE_ENV === 'production') {
  if ('serviceWorker' in navigator) {
     navigator.serviceWorker.register('offline-worker.js')
       .then((registration) => {
         console.log('ServiceWorker registration successful with scope: ', registration.scope);
       })
       .catch((err) => {
         console.log('ServiceWorker registration failed: ', err);
       });
  }
}
