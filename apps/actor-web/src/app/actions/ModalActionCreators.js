'use strict';

var ActorAppDispatcher = require('../dispatcher/ActorAppDispatcher');
var ActorAppConstants = require('../constants/ActorAppConstants');
var ActionTypes = ActorAppConstants.ActionTypes;

var ModalActionCreators = {
  show: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.SHOW_MODAL,
    })
  },

  hide: function() {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.HIDE_MODAL
    })
  }
};

module.exports = ModalActionCreators;
