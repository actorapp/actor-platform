var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

var LoginActionCreators = {
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
            log.error('Unsupported state', state);
        }
      },
      () => {
      });
  },

  sendSignup: (router, name) => {
    ActorClient.signUp(name, () => {
      LoginActionCreators.setLoggedIn(router);
    })
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
    })
  }
};

module.exports = LoginActionCreators;
