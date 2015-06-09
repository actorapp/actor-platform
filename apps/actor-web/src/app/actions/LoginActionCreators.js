import ActorClient from '../utils/ActorClient';

import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import ActorAppConstants from '../constants/ActorAppConstants';
import LoginActionCreators from '../actions/LoginActionCreators';

var ActionTypes = ActorAppConstants.ActionTypes;

export default {
  requestSms: function (phone) {
    ActorClient.requestSms(phone, function () {
      ActorAppDispatcher.dispatch({
        type: ActionTypes.AUTH_SMS_REQUESTED
      });
    });
  },

  sendCode: function (router, code) {
    ActorClient.sendCode(code,
      (state) => {
        switch (state) {
          case 'signup':
            ActorAppDispatcher.dispatch({
              type: ActionTypes.START_SIGNUP
            });

            break;
          case 'logged_in':
            LoginActionCreators.setLoggedIn(router);
            break;
          default:
            console.error('Unsupported state', state);
        }
      },
      () => {
      });
  },

  sendSignup: (router, name) => {
    ActorClient.signUp(name, () => {
      LoginActionCreators.setLoggedIn(router);
    });
  },

  setLoggedIn: function (router) {
    var nextPath = router.getCurrentQuery().nextPath;

    if (nextPath) {
      router.replaceWith(nextPath);
    } else {
      router.replaceWith('/');
    }

    ActorAppDispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    });
  }
};
