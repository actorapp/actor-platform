import ActorAppDispatcher from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const FaviconActionCreators = {
  setDefaultFavicon() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.FAVICON_SET_DEFAULT
    });
  },

  setNotificationIcon() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.FAVICON_SET_NOTIFICATION
    });
  }
};

window.setNotificationFavicon = () => {
  if (document.hidden) {
    FaviconActionCreators.setNotificationIcon();
  }
};

export default FaviconActionCreators;
