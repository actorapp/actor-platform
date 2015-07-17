class AppCache {
  constructor() {

    window.addEventListener('load', () => {

      window.applicationCache.addEventListener('updateready', this.onUpdateReady, false);

      if (window.applicationCache.status === window.applicationCache.UPDATEREADY) {
        this.onUpdateReady();
      }

      // Check applications cache for update every 10 mins.
      setInterval(() => {
        window.applicationCache.update();
      }, 600000);

    }, false);

  }

  onUpdateReady() {

    window.applicationCache.swapCache();

    if (confirm('A new version of Actor Web App is available. Load it?')) {
      window.location.reload();
    }

  }
}

const AppCacheInstance = new AppCache();

export default AppCacheInstance;
