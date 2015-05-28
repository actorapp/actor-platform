var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {
  requestSms: function(phone) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.REQUEST_SMS,
      phone: phone
    });
  },

  sendCode: function(code) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SEND_CODE,
      code: code
    });
  },

  setLoggedIn: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    });
  }
};
