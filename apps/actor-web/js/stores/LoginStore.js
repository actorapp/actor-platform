var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var LoginActionCreators = require('../actions/LoginActionCreators');

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var LOGIN_EVENT = 'login';

var LoginStore = assign({}, EventEmitter.prototype, {
  emitLogin: function() {
    this.emit(LOGIN_EVENT);
  },

  addLoginListener: function(callback) {
    this.on(LOGIN_EVENT, callback);
  },

  removeLoginListener: function(callback) {
    this.removeListener(LOGIN_EVENT, callback);
  }
});

LoginStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.SET_LOGGED_IN:
      LoginStore.emitLogin();
      break;
    case ActionTypes.REQUEST_SMS:
      ActorClient.requestSms(action.phone, action.callback);
      break;
    default:

  }
});

module.exports = LoginStore;
