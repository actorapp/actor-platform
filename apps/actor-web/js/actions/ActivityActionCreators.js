'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var ActivityActionCreators = {
  show: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SHOW_ACTIVITY
    })
  },

  hide: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.HIDE_ACTIVITY
    })
  }
};

module.exports = ActivityActionCreators;
