var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var LoginActionCreators = require('../actions/LoginActionCreators');

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _isSmsRequested = false;

var LoginStore = assign({}, EventEmitter.prototype, {
  isLoggedIn: function() {
    return(ActorClient.isLoggedIn());
  },

  isSmsRequested: function() {
    return(_isSmsRequested);
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }
});

LoginStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.AUTH_SMS_REQUESTED:
      _isSmsRequested = true;
      LoginStore.emitChange();
      break;
    case ActionTypes.SET_LOGGED_IN:
      LoginStore.emitChange();
      break;
    default:

  }
});

module.exports = LoginStore;
