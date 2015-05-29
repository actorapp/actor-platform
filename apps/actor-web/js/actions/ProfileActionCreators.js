'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var ProfileActionCreators = {
  clickUser: function(userId) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CLICK_USER,
      userId: userId
    });
  },

  clickGroup: function(groupId) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.CLICK_GROUP,
      groupId: groupId
    });
  }
};

module.exports = ProfileActionCreators;
