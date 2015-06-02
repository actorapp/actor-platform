'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActorClient = require('../utils/ActorClient');
var ActionTypes = ActorAppConstants.ActionTypes;
var ActivityTypes = ActorAppConstants.ActivityTypes;

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _activity = {
  type: 'default'
};

var ActivityStore = assign({}, EventEmitter.prototype, {
  getActivity: function() {
    return(_activity);
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback)
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback)
  }
});

var _cleanup = function() {

};

ActivityStore.dispatchToken = ActorAppDispatcher.register(function(action) {
  switch(action.type) {
    case ActionTypes.CLICK_USER:
      _cleanup();

      var change = function(user) {
        _activity = {
          type: ActivityTypes.USER_PROFILE,
          userId: action.userId,
          user: user
        };

        ActivityStore.emitChange();
      };

      _cleanup = function() {
        ActorClient.unbindUser(action.userId, change);
      };

      ActorClient.bindUser(action.userId, change);

      break;
    case ActionTypes.CLICK_GROUP:
      var group = ActorClient.getGroup(action.groupId);
      _activity = {
        type: ActivityTypes.GROUP_PROFILE,
        groupId: action.groupId,
        group: group
      };
      ActivityStore.emitChange();
      break;
    default:
  }
});

module.exports = ActivityStore;
