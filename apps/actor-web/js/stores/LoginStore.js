var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

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
    default:

  }
});

module.exports = LoginStore;
