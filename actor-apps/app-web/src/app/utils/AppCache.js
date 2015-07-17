import AppCacheActionCreators from 'actions/AppCacheActionCreators';
//import AppCacheStore from 'stores/AppCacheStore';

class AppCache {
  constructor() {

    window.addEventListener('load', () => {

      window.applicationCache.addEventListener('updateready', this.onUpdateReady);

      // Check applications cache for update every 10 mins.
      setInterval(() => {
        window.applicationCache.update();
      }, 600000);

    }, false);

  }

  onUpdateReady() {

    window.applicationCache.swapCache();

    AppCacheActionCreators.openModal();
  }
}

const AppCacheInstance = new AppCache();

export default AppCacheInstance;
