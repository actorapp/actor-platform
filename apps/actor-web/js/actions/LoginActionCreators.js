var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

var LoginActionCreators = {
  requestSms: function(phone) {
    ActorClient.requestSms(phone, function() {
      ActorAppDispatcher.dispatch({
        type: ActionTypes.AUTH_SMS_REQUESTED
      });
    });
  },

  sendCode: function(code) {
    ActorClient.sendCode(code, function() {
      LoginActionCreators.setLoggedIn();
    });
  },

  setLoggedIn: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    })
  }
};

module.exports = LoginActionCreators;
