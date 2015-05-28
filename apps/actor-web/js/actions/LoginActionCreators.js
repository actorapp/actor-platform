var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

module.exports = {
  requestSms: function(phone, callback) {
    ActorClient.requestSms(phone, callback);
  },

  sendCode: function(code, callback) {
    ActorClient.sendCode(code, callback);
  },

  setLoggedIn: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SET_LOGGED_IN
    });
  }
};
