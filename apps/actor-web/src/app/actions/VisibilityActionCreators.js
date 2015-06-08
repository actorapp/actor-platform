'use strict';

var ActorClient = require('../utils/ActorClient');

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');

var ActionTypes = ActorAppConstants.ActionTypes;

var VisibilityActionCreators = {
  createAppVisible: function() {
    ActorClient.onAppVisible();

    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_VISIBLE
    });
  },

  createAppHidden: function() {
    ActorClient.onAppHidden();

    ActorAppDispatcher.dispatch({
      type: ActionTypes.APP_HIDDEN
    });
  }
};

module.exports = VisibilityActionCreators;
